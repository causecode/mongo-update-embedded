/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo.embeddable

import com.causecode.validatable.BaseTestSetup
import grails.test.mixin.Mock
import org.bson.types.ObjectId
import spock.lang.Specification
import test.EmTestDomainA
import test.EmTestDomainB
import test.EmTestEmailDomain
import test.TestDomainA
import test.TestDomainC

/**
 * Test cases for TestDomainA class.
 */
@Mock([TestDomainA, TestDomainC])
class EmbeddableDomainSpec extends Specification implements BaseTestSetup {

    TestDomainA testDomainAInstance

    void setup() {
        TestDomainA testDomainAInstance = new TestDomainA()
        testDomainAInstance.testField1 = 'Test 1'
        testDomainAInstance.testField2 = 'Test 2'
        testDomainAInstance.embeddingNonDomainField = new ObjectId()
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.id

        this.testDomainAInstance = testDomainAInstance
    }

    void 'test resolveParentDomainClass method to return parent domain class name'() {
        when: 'The resolveParentDomainClass method is called'
        String domainName1 = EmTestDomainA.resolveParentDomainClass()
        String domainName2 = EmTestEmailDomain.resolveParentDomainClass()

        then: 'The name of the parent domain class with Em prefix truncated should be received.'
        domainName1 == 'TestDomainA'
        domainName2 == 'TestEmailDomain'
    }

    void 'test dynamic getActualInstance method'() {
        given: 'Embedded instance of TestDomainA'
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        when: 'The getActualInstance method is called on the embedded instance'
        TestDomainA testDomainA = emTestDomainA.actualInstance

        then: 'Actual TestDomainA instance should be received'
        testDomainA == testDomainAInstance

        when: 'The propery actualInstance is accessed'
        TestDomainA testDomainA2 = emTestDomainA.actualInstance

        then: 'Actual TestDomainA instance should be received'
        testDomainA2 == testDomainAInstance
    }

    void 'test toMap method to return Embeddable domain class object as a map'() {
        given: 'Embedded instance of TestDomainA'
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        when: 'The toMap method is called'
        Map objectToMap = emTestDomainA.toMap()

        then: 'All fields should be present in the map'
        objectToMap == [instanceId: testDomainAInstance.id, testField1: testDomainAInstance.testField1, status: 1]

        when: 'The toMap method and the object has another nested object'

        EmTestDomainB emTestDomainB = new EmTestDomainB([testB: 'Test B', testDomainA: emTestDomainA])
        objectToMap = emTestDomainB.toMap()

        then: 'The map should have nested object'
        objectToMap == [testB: 'Test B', testDomainA: emTestDomainA.toMap()]
    }

    void 'test Embeddable domain to throw exception for property or method other than actualInstance'() {
        given: 'Embedded instance of TestDomainA'
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        when: 'Any random property is accessed'
        emTestDomainA.random

        then: 'MissingPropertyException should be thrown'
        thrown(MissingPropertyException)

        when: 'Any random method is accessed'
        emTestDomainA.random()

        then: 'MissingMethodException should be thrown'
        thrown(MissingMethodException)
    }

    // For the coverage of null check in equals method, explicitly calling the equals method instead of '=='
    @SuppressWarnings(['ExplicitCallToEqualsMethod'])
    void 'test equals method for matching the instanceId'() {
        given: 'Embedded instance of TestDomainA'
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        expect:
        // Matching with null will return false
        !(emTestDomainA.equals(null))

        // Matching with other class will return false
        !(emTestDomainA == testDomainAInstance)

        // Matching with same class object with same instanceId will return true
        emTestDomainA == new EmTestDomainA([instanceId: testDomainAInstance.id])

        // Matching with same class but different instanceId will return false
        !(emTestDomainA == new EmTestDomainA())
    }

    void 'test toMap method when there is a Collection type present in the Domain class'() {
        given: 'An instance of TestDomainC'
        TestDomainC testDomainCInstance = createTestDomainC(testDomainAInstance.embeddedInstance)

        when: 'toMap method is called'
        Map instancePropertiesMap = testDomainCInstance.embeddedInstance.toMap()

        then: 'Following conditions must be satisfied'
        instancePropertiesMap.instanceId == testDomainCInstance.id
        instancePropertiesMap.name == testDomainCInstance.name

        instancePropertiesMap.collectionTypeListOfString == testDomainCInstance.collectionTypeListOfString
        instancePropertiesMap.collectionTypeListOfInteger == testDomainCInstance.collectionTypeListOfInteger

        instancePropertiesMap.collectionTypeSetOfInteger as Set == testDomainCInstance.collectionTypeSetOfInteger as Set
        instancePropertiesMap.collectionTypeSetOfString as Set == testDomainCInstance.collectionTypeSetOfString as Set

        instancePropertiesMap.collectionTypeListOfEnum.eachWithIndex { String car, int index ->
            assert car == testDomainCInstance.collectionTypeListOfEnum[index].name()
        }

        instancePropertiesMap.collectionTypeListOfObjects.eachWithIndex { Map map, int index ->
            assert map.instanceId == testDomainCInstance.collectionTypeListOfObjects[index].instanceId
            assert map.testField1 == testDomainCInstance.collectionTypeListOfObjects[index].testField1
            assert map.status == testDomainCInstance.collectionTypeListOfObjects[index].status.id
        }
    }
}
