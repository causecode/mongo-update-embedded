/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.bson.types.ObjectId
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges
import test.TestDomainA
import test.TestDomainB

@Integration
@Rollback
class EmbeddedInstanceQueueServiceSpec extends Specification {

    EmbeddedInstanceQueueService embeddedInstanceQueueService

    Object logStatement

    void setup() {
        // Mocking the logger calls to test the log statements.
        embeddedInstanceQueueService.log = [debug: { Object message ->
            logStatement = message
        }, info: { Object message ->
            logStatement = message
        }, error: { Object message, Throwable e = new Exception() ->
            logStatement = message
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

        and: 'Creating instance with above map to verify that instance has errors'
        propertyMap.sourceDomain = 'TestDomainA'
        EmbeddedInstanceQueue embeddedInstanceQueueInstance = new EmbeddedInstanceQueue(propertyMap)
        embeddedInstanceQueueInstance.save()

        assert embeddedInstanceQueueInstance.hasErrors()

        when: "The addToQueue method is called with arguments domainToUpdate, fieldToUpdate, isFieldArray"
        embeddedInstanceQueueService.addToQueue(propertyMap.domainToUpdate, [fieldName: propertyMap.fieldToUpdate,
                isFieldArray: propertyMap.isFieldArray], new TestDomainA())

        then: "New instance will not be created"
        EmbeddedInstanceQueue.count() == 0
        logStatement == "Failed to create EmbeddedInstanceQueue instance due to ${embeddedInstanceQueueInstance.errors}"
    }

    void "test processEmbeddedInstanceQueue method to process all active instances of EmbeddedInstanceQueue"() {
        given: "An EmbeddedInstanceQueue instance with status ACTIVE"
        EmbeddedInstanceQueue embeddedInstanceQueueInstance = createEmbeddedInstanceQueue()

        assert embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.ACTIVE

        when: "The processEmbeddedInstanceQueue method is called to process the instance"
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

        when: "The processEmbeddedInstanceQueue method is called to process the instance"
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: "It should throw NullPointerException and the logStatement should reflect the error"
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

        and: "The TestDomainA is updated"
        testDomainAInstance.testField1 = "Update test field 1"
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.testField1 == "Update test field 1"
        assert testDomainBInstance.id
        assert testDomainBInstance.testDomainA.testField1 == "Test 1"

        assert EmbeddedInstanceQueue.count() == 2

        when: 'The processEmbeddedInstanceQueue method is called to process the instance'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: "The embedded instance within TestDomainB should be updated"
        testDomainBInstance.refresh().testDomainA.testField1 == "Update test field 1"
    }

    @ConfineMetaClassChanges([TestDomainB])
    void "test for marking EmbeddedQueueInstance's status as FAILED when 3 attempts fails to update it"() {
        given: "An instance of TestDomainA"
        TestDomainA testDomainAInstance = createTestDomainA()
        TestDomainB testDomainBInstance = new TestDomainB([name: "Test B", title: "B title",
                testDomainA: testDomainAInstance.embeddedInstance,
                testDomainASet: [testDomainAInstance.embeddedInstance] as Set])
        testDomainBInstance.save(flush: true)

        assert testDomainBInstance.toString() == "TestDomainB (${testDomainBInstance.id})".toString()

        and: 'Mocked lower level update call to throw exception'
        TestDomainB.metaClass.'static'.getCollection = {
            return ['update' : { Map query, Map update, Map options ->
                throw new Exception()
            }]
        }

        and: "The test domain is updated"
        testDomainAInstance.testField1 = "Update test field 1"
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.testField1 == "Update test field 1"
        assert testDomainBInstance.id
        assert testDomainBInstance.testDomainA.testField1 == "Test 1"

        assert EmbeddedInstanceQueue.count() == 2

        EmbeddedInstanceQueue embeddedInstanceQueueInstance = EmbeddedInstanceQueue.first()
        assert embeddedInstanceQueueInstance.attemptCount == 0

        when: 'The processEmbeddedInstanceQueue method is called first time'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: 'The attemptCount is increased to one but status remains ACTIVE'
        embeddedInstanceQueueInstance.attemptCount == 1
        embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.ACTIVE

        when: 'The processEmbeddedInstanceQueue method is called second time'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: 'The attemptCount is increased to two but status remains ACTIVE'
        embeddedInstanceQueueInstance.attemptCount == 2
        embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.ACTIVE

        when: 'The processEmbeddedInstanceQueue method is called third time'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: 'The attemptCount is increased to three but status remains ACTIVE'
        embeddedInstanceQueueInstance.attemptCount == 3
        embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.ACTIVE

        when: 'The processEmbeddedInstanceQueue method is called fourth time'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: 'The attemptCount is increased to four and status changes to FAILED'
        embeddedInstanceQueueInstance.attemptCount == 4
        embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.FAILED

        when: 'Status changed to FAILED and processEmbeddedInstanceQueue is called the next time'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: 'No records should be fetched'
        logStatement == "Found [0] records to update embedded instances"
    }

    @Unroll
    void "test processEmbeddedInstanceQueue to return 0 records when attemptCount #attemptCount, status is #status"() {
        given: "An EmbeddedInstanceQueue instance with status ACTIVE and attemptCount 0"
        EmbeddedInstanceQueue embeddedInstanceQueueInstance = createEmbeddedInstanceQueue()

        assert embeddedInstanceQueueInstance.attemptCount == 0
        assert embeddedInstanceQueueInstance.status == EmbeddedInstanceQueueStatus.ACTIVE

        and: 'attemptCount and status is modified to test failure condition'
        embeddedInstanceQueueInstance.attemptCount = attemptCount
        embeddedInstanceQueueInstance.status = status
        embeddedInstanceQueueInstance.save(flush: true)

        when: "The processEmbeddedInstanceQueue method is called to process the instance"
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()

        then: "There should be zero records to process."
        logStatement == "Found [0] records to update embedded instances"

        where:
        attemptCount | status
        0            | EmbeddedInstanceQueueStatus.FAILED
        4            | EmbeddedInstanceQueueStatus.ACTIVE
    }
}
