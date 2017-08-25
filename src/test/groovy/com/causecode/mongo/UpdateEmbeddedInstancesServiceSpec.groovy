/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo

import com.causecode.validatable.BaseTestSetup
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.bson.types.ObjectId
import spock.lang.Specification
import test.EmTestDomainA

import test.TestDomainA
import test.TestDomainB
import test.TestDomainC

/**
 * Test cases for UpdateEmbeddedInstancesService class.
 */
@TestFor(UpdateEmbeddedInstancesService)
@Mock([TestDomainA, TestDomainB, TestDomainC, EmbeddedInstanceQueueService, EmbeddedInstanceQueue])
class UpdateEmbeddedInstancesServiceSpec extends Specification implements BaseTestSetup {

    void 'test initializeEmbeddedDomainsMap for creating correct maps for TestDomainA and TestDomainB'() {
        when: 'The getEmbeddedDomainsMap method is called'
        service.embeddedDomainsMap

        then: 'The domainsThatEmbed and embeddedClassFields map should be correctly initialized'
        UpdateEmbeddedInstancesService.domainsThatEmbed == [TestDomainA: [TestDomainB: [[fieldName: 'testDomainA',
                isFieldArray: false], [fieldName: 'testDomainASet', isFieldArray: true]]]]
        UpdateEmbeddedInstancesService.embeddedClassFields == [TestDomainA: [fieldList: ['testField1', 'status']]]
    }

    void 'test resolvePrivateFieldNames for returning field names that needs to be matched for dirty checking'() {
        when: 'The resolveFieldsForDirtiness method is called for EmTestDomainA'
        List<String> fieldNames = service.resolveFieldsForDirtiness(EmTestDomainA)

        then: 'The field TestFieldA should be returned'
        fieldNames == ['testField1', 'status']
    }

    void 'test getFieldsToCheckForDirty to get the list of fields to check dirty for a domain'() {
        given: 'Initializing the field list for each domain'
        service.embeddedDomainsMap

        when: 'The getFieldsToCheckForDirty method is called for TestDomainA'
        List<String> fieldNames = service.getFieldsToCheckForDirty(TestDomainA.simpleName)

        then: 'The field TestFieldA should be returned'
        fieldNames == ['testField1', 'status']
    }

    void 'test enqueue for adding EmbeddedInstanceQueue instances for a domain instance'() {
        given: 'An instance of TestDomainA'
        TestDomainA testDomainAInstance = new TestDomainA()
        testDomainAInstance.testField1 = 'Test 1'
        testDomainAInstance.testField2 = 'Test 2'
        testDomainAInstance.embeddingNonDomainField = new ObjectId()
        testDomainAInstance.save(flush: true)

        assert EmbeddedInstanceQueue.count() == 0

        when: 'The enqueue method is called for this domain instance'
        service.enqueue(testDomainAInstance)

        then: 'Instances of EmbeddedInstanceQueue should be created'
        List<EmbeddedInstanceQueue> embeddedInstanceQueueList = EmbeddedInstanceQueue.list(sort: 'dateCreated', max: 10)

        embeddedInstanceQueueList.size() == 2

        embeddedInstanceQueueList[0].domainToUpdate == 'TestDomainB'
        embeddedInstanceQueueList[0].fieldToUpdate == 'testDomainA'
        !embeddedInstanceQueueList[0].isFieldArray
        embeddedInstanceQueueList[0].sourceDomain == 'TestDomainA'
        embeddedInstanceQueueList[0].sourceDomainId == testDomainAInstance.id

        embeddedInstanceQueueList[1].domainToUpdate == 'TestDomainB'
        embeddedInstanceQueueList[1].fieldToUpdate == 'testDomainASet'
        !embeddedInstanceQueueList[1].isFieldArray
        embeddedInstanceQueueList[1].sourceDomain == 'TestDomainA'
        embeddedInstanceQueueList[1].sourceDomainId == testDomainAInstance.id
    }

    void "test resolveFieldsForDirtiness method"() {
        given: 'Instances of TestDomainA and TestDomainC'
        TestDomainA testDomainA = createTestDomainA()
        TestDomainC testDomainC = createTestDomainC(testDomainA.embeddedInstance)

        when: 'resolveFieldsForDirtiness method is called with embedded instance of testDomainC'
        List<String> listOfResolvedFields = service.resolveFieldsForDirtiness(testDomainC.embeddedInstance.class)

        then: 'The following conditions must be satisfied'
        listOfResolvedFields.size() == 8
        ['name', 'collectionTypeListOfString', 'collectionTypeSetOfString',
         'collectionTypeListOfInteger', 'collectionTypeSetOfInteger', 'collectionTypeListOfEnum',
         'collectionTypeListOfObjects', 'mapOfString'] == listOfResolvedFields
    }
}
