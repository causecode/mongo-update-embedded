/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.bson.types.ObjectId
import org.slf4j.Logger
import spock.lang.Specification
import test.TestDomainA
import test.TestDomainB

@Integration
@Rollback
class EmbeddedInstanceQueueServiceSpec extends Specification {

    EmbeddedInstanceQueueService embeddedInstanceQueueService

    def logStatement

    void setup() {
        // Mocking the logger calls to test the log statements.
        embeddedInstanceQueueService.log = [debug: { Object message ->
            logStatement = message
        }, info: { Object message ->
            logStatement = message
        }, error: { Object message, Throwable e = new Exception() ->
            logStatement = message
            println e.message
        } ] as Logger
    }

    TestDomainA createTestDomainA() {
        TestDomainA testDomainAInstance = new TestDomainA()
        testDomainAInstance.testField1 = "Test 1"
        testDomainAInstance.testField2 = "Test 2"
        testDomainAInstance.embeddingNonDomainField = new ObjectId()
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.id

        return testDomainAInstance
    }

    EmbeddedInstanceQueue createEmbeddedInstanceQueue() {
        Map propertyMap = [domainToUpdate: 'TestDomainB', fieldToUpdate: 'testDomainA', isFieldArray:false,
                           sourceDomain: 'TestDomainA', sourceDomainId: createTestDomainA().id]

        EmbeddedInstanceQueue embeddedInstanceQueueInstance = new EmbeddedInstanceQueue(propertyMap)
        embeddedInstanceQueueInstance.save(flush :true)

        assert embeddedInstanceQueueInstance.id

        return embeddedInstanceQueueInstance
    }

    void "test addToQueue method when EmbeddedInstanceQueue instance already exist"() {
        given: "An instance of TestDomainA and EmbeddedInstanceQueue"
        EmbeddedInstanceQueue embeddedInstanceQueueInstance = createEmbeddedInstanceQueue()

        assert EmbeddedInstanceQueue.count() == 1

        when: "The addToQueue method is called with same properties"
        embeddedInstanceQueueService.addToQueue(embeddedInstanceQueueInstance.domainToUpdate,
                [fieldName: embeddedInstanceQueueInstance.fieldToUpdate,
                 isFieldArray: embeddedInstanceQueueInstance.isFieldArray], TestDomainA.first())

        then: "New instance will not be created"
        EmbeddedInstanceQueue.count() == 1
        EmbeddedInstanceQueue.first() == embeddedInstanceQueueInstance
        logStatement == "Found active record for [${embeddedInstanceQueueInstance.domainToUpdate}] from " +
                "[${embeddedInstanceQueueInstance.sourceDomain}]"
    }

    void "test addToQueue method when EmbeddedInstanceQueue's property map is incorrect"() {
        given: "Property map for EmbeddedInstanceQueue"
        Map propertyMap = [domainToUpdate: 'TestDomainB', fieldToUpdate: 'testDomainA', isFieldArray: false]

        assert EmbeddedInstanceQueue.count() == 0

        when: "The addToQueue method is called with these properties"
        embeddedInstanceQueueService.addToQueue(propertyMap.domainToUpdate, [fieldName: propertyMap.fieldToUpdate, isFieldArray:
                propertyMap.isFieldArray], new TestDomainA())

        then: "New instance will not be created"
        EmbeddedInstanceQueue.count() == 0
        logStatement == 'Failed to create EmbeddedInstanceQueue instance.'
    }

    void "test processEmbeddedInstanceQueue method to process all active instances of EmbeddedInstanceQueue"() {
        given: "An EmbeddedInstanceQueue instance with status ACTIVE"
        EmbeddedInstanceQueue embeddedInstanceQueueInstance = createEmbeddedInstanceQueue()

        assert embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.ACTIVE

        when: "The processEmbeddedInstanceQueue method is called"
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: "The instance's status should be updated to PROCESSED"
        embeddedInstanceQueueInstance.refresh().status == EmbeddedInstanceQueueStatus.PROCESSED
    }

    void "test processEmbeddedInstanceQueue method for handling exceptions"() {
        given: "An instance of TestDomainA and EmbeddedInstanceQueue"
        TestDomainA testDomainAInstance = createTestDomainA()
        EmbeddedInstanceQueue embeddedInstanceQueueInstance = createEmbeddedInstanceQueue()

        and: "The EmbeddedInstanceQueue instance's domainToUpdate field is changed to a non domain class"
        embeddedInstanceQueueInstance.domainToUpdate = 'TestClass'
        embeddedInstanceQueueInstance.save(flush: true)

        when: "There should be null pointer exception when trying to process the instance"
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: "The logStatement should reflect the error"
        logStatement == "Error updating queued embedded instance [$embeddedInstanceQueueInstance]"
    }

    void "test complete cycle for PreUpdateEventListener when a domain instance is updated"() {
        given: "An instance of TestDomainA"
        TestDomainA testDomainAInstance = createTestDomainA()
        TestDomainB testDomainBInstance = new TestDomainB([name: "Test B", title: "B title",
                testDomainA: testDomainAInstance.embeddedInstance,
                testDomainASet: [testDomainAInstance.embeddedInstance] as Set])
        testDomainBInstance.save(flush: true)

        assert testDomainBInstance.toString() == "TestDomainB (${testDomainBInstance.id})".toString()

        when: "The test domain is updated"
        testDomainAInstance.testField1 = "Update test field 1"
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.testField1 == "Update test field 1"
        assert testDomainBInstance.id
        assert testDomainBInstance.testDomainA.testField1 == "Test 1"

        assert EmbeddedInstanceQueue.count() == 2
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: "The embedded instance within TestDomainB should be updated"
        testDomainBInstance.refresh().testDomainA.testField1 == "Update test field 1"
    }
}
