/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo.embeddable

import grails.test.mixin.Mock
import org.bson.types.ObjectId
import spock.lang.Specification
import test.EmTestDomainA
import test.EmTestDomainB
import test.TestDomainA

@Mock([TestDomainA])
class EmbeddableDomainSpec extends Specification {

    TestDomainA testDomainAInstance

    void setup() {
        TestDomainA testDomainAInstance = new TestDomainA()
        testDomainAInstance.testField1 = "Test 1"
        testDomainAInstance.testField2 = "Test 2"
        testDomainAInstance.embeddingNonDomainField = new ObjectId()
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.id

        this.testDomainAInstance = testDomainAInstance
    }

    void "test resolveParentDomainClass method to return parent domain class name"() {
        when: "The resolveParentDomainClass method is called"
        String domainName = EmTestDomainA.resolveParentDomainClass()

        then: "TestDomainA should be received"
        domainName == 'TestDomainA'
    }

    void "test dynamic getActualInstance method"() {
        given: "Embedded instance of TestDomainA"
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        when: "The getActualInstance method is called on the embedded instance"
        TestDomainA testDomainA = emTestDomainA.getActualInstance()

        then: "Actual TestDomainA instance should be received"
        testDomainA == testDomainAInstance

        when: "The propery actualInstance is accessed"
        TestDomainA testDomainA2 = emTestDomainA.actualInstance

        then: "Actual TestDomainA instance should be received"
        testDomainA2 == testDomainAInstance
    }

    void "test toMap method to return Embeddable domain class's object as a map"() {
        given: "Embedded instance of TestDomainA"
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        when: "The toMap method is called"
        Map objectToMap = emTestDomainA.toMap()

        then: "All fields should be present in the map"
        objectToMap == [instanceId: testDomainAInstance.id, testField1: testDomainAInstance.testField1, status: 1]

        when: "The toMap method and the object has another nested object"

        EmTestDomainB emTestDomainB = new EmTestDomainB([testB: "Test B", testDomainA: emTestDomainA])
        objectToMap = emTestDomainB.toMap()

        then: "The map should have nested object"
        objectToMap == [testB: "Test B", testDomainA: emTestDomainA.toMap()]
    }

    void "test Embeddable domain to throw exception for property or method other than actualInstance"() {
        given: "Embedded instance of TestDomainA"
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        when: "Any random property is accessed"
        emTestDomainA.random

        then: "MissingPropertyException should be thrown"
        thrown(MissingPropertyException)

        when: "Any random method is accessed"
        emTestDomainA.random()

        then: "MissingMethodException should be thrown"
        thrown(MissingMethodException)
    }

    void "test equals method for matching the instanceId"() {
        given: "Embedded instance of TestDomainA"
        EmTestDomainA emTestDomainA = testDomainAInstance.embeddedInstance

        expect:
        // Matching with null will return false
        !emTestDomainA.equals(null)

        // Matching with other class will return false
        !emTestDomainA.equals(testDomainAInstance)

        // Matching with same class object with same instanceId will return true
        emTestDomainA.equals(new EmTestDomainA([instanceId: testDomainAInstance.id]))

        // Matching with same class but different instanceId will return false
        !emTestDomainA.equals(new EmTestDomainA())
    }
}
