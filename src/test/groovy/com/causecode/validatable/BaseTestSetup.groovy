/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.validatable

import org.bson.types.ObjectId
import test.Car
import test.EmTestDomainA
import test.TestDomainA
import test.TestDomainC

/**
 * A Trait which contains utility method to generate test instance.
 *
 * @author Hardik Modha
 * @since 0.0.7
 */
trait BaseTestSetup {

    TestDomainA createTestDomainA() {
        TestDomainA testDomainAInstance = new TestDomainA()
        testDomainAInstance.testField1 = 'Test 1'
        testDomainAInstance.testField2 = 'Test 2'
        testDomainAInstance.embeddingNonDomainField = new ObjectId()
        testDomainAInstance.save(flush: true)

        assert testDomainAInstance.id

        return testDomainAInstance
    }

    TestDomainC createTestDomainC(EmTestDomainA emTestDomainA) {
        List<String> listOfCars = ['Bugatti', 'Maserati', '']
        Set<String> setOfCars = ['Mercedes', 'Koenigsegg'] as Set
        List<Integer> listOfCarCost = [159600032, 59876556]
        Set<Integer> setOfCarCost = [79461356, 784545752] as Set
        List<Car> listOfCarEnums = [Car.BUGATTI, Car.MASERATI, Car.KOENIGSEGG]
        List<EmTestDomainA> listOfObjects = [emTestDomainA, emTestDomainA]
        Map<String, String> mapOfString = [name: 'Bugatti', speed: '432 km/h']

        TestDomainC testDomainCInstance = new TestDomainC(
                name: 'Dummy',
                collectionTypeListOfString: listOfCars,
                collectionTypeSetOfString: setOfCars,
                collectionTypeListOfInteger: listOfCarCost,
                collectionTypeSetOfInteger: setOfCarCost,
                collectionTypeListOfEnum: listOfCarEnums,
                collectionTypeListOfObjects: listOfObjects,
                mapOfString: mapOfString,
        )

        testDomainCInstance.save(flush: true, failOnError: true)

        assert testDomainCInstance.id

        return testDomainCInstance
    }
}
