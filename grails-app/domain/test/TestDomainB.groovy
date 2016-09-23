/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package test

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

/**
 * A test domain class for writing test cases. This will be excluded from plugin while packaging.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
@EqualsAndHashCode
class TestDomainB {

    ObjectId id

    String name
    String title

    EmTestDomainA testDomainA
    Set<EmTestDomainA> testDomainASet

    static embedded = ['testDomainA', 'testDomainASet']

    static constraints = {
    }

    @Override
    String toString() {
        return "TestDomainB ($id)"
    }
}