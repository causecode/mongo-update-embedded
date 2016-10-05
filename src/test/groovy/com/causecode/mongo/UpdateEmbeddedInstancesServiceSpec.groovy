/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.bson.types.ObjectId
import spock.lang.Specification
import test.EmTestDomainA

import test.TestDomainA
import test.TestDomainB

@TestFor(UpdateEmbeddedInstancesService)
@Mock([TestDomainA, TestDomainB, EmbeddedInstanceQueueService, EmbeddedInstanceQueue])
class UpdateEmbeddedInstancesServiceSpec extends Specification {

    void "test initializeEmbeddedDomainsMap for creating correct maps for TestDomainA and TestDomainB"() {
        when: "The initializeEmbeddedDomainsMap method is called"
        service.initializeEmbeddedDomainsMap()

        then: "The domainsThatEmbed and embeddedClassFields map should be correctly initialized"
        UpdateEmbeddedInstancesService.domainsThatEmbed == [TestDomainA: [TestDomainB: [[fieldName: 'testDomainA',
                isFieldArray: false], [fieldName: 'testDomainASet', isFieldArray: true]]]]
        UpdateEmbeddedInstancesService.embeddedClassFields == [TestDomainA: [fieldList: ['testField1', 'status']]]
    }

    void "test resolvePrivateFieldNames for returning field names that needs to be matched for dirty checking"() {
        when: "The resolvePrivateFieldNames method is called for EmTestDomainA"
        List<String> fieldNames = service.resolvePrivateFieldNames(EmTestDomainA)

        then: "The field TestFieldA should be returned"
        fieldNames == ['testField1', 'status']
    }

    void "test getFieldsToCheckForDirty to get the list of fields to check dirty for a domain"() {
        given: "Initializing the field list for each domain"
        service.initializeEmbeddedDomainsMap()

        when: "The getFieldsToCheckForDirty method is called for TestDomainA"
        List<String> fieldNames = service.getFieldsToCheckForDirty(TestDomainA.class.simpleName)

        then: "The field TestFieldA should be returned"
        fieldNames == ['testField1', 'status']
    }

    void "test addToUpdateQueue for adding EmbeddedInstanceQueue instances for a domain instance"() {
        given: "An instance of TestDomainA"
        TestDomainA testDomainAInstance = new TestDomainA()
        testDomainAInstance.testField1 = "Test 1"
        testDomainAInstance.testField2 = "Test 2"
        testDomainAInstance.embeddingNonDomainField = new ObjectId()
        testDomainAInstance.save(flush: true)

        assert EmbeddedInstanceQueue.count() == 0

        when: "The addToUpdateQueue method is called for this domain instance"
        service.addToUpdateQueue(testDomainAInstance)

        then: "Instances of EmbeddedInstanceQueue should be created"
        List<EmbeddedInstanceQueue> embeddedInstanceQueueList = EmbeddedInstanceQueue.list(sort: 'dateCreated')

        embeddedInstanceQueueList[0].domainToUpdate == 'TestDomainB'
        embeddedInstanceQueueList[0].fieldToUpdate == 'testDomainA'
        embeddedInstanceQueueList[0].isFieldArray == false
        embeddedInstanceQueueList[0].sourceDomain == 'TestDomainA'
        embeddedInstanceQueueList[0].sourceDomainId == testDomainAInstance.id

        embeddedInstanceQueueList[1].domainToUpdate == 'TestDomainB'
        embeddedInstanceQueueList[1].fieldToUpdate == 'testDomainASet'
        embeddedInstanceQueueList[1].isFieldArray == true
        embeddedInstanceQueueList[1].sourceDomain == 'TestDomainA'
        embeddedInstanceQueueList[1].sourceDomainId == testDomainAInstance.id
    }
}
