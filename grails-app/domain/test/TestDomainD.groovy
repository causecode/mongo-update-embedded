/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package test

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.bson.types.ObjectId

/**
 * This is a test domain class which embeds the {@link EmTestDomainC}.
 *
 * @author Hardik Modha
 * @since 0.0.7
 */
@EqualsAndHashCode
@ToString(includes = ['id'], includePackage = false)
class TestDomainD {

    ObjectId id

    EmTestDomainC emTestDomainC

    Date dateCreated
    Date lastUpdated

    static embedded = ['emTestDomainC']

    static constraints = {
        dateCreated bindable: false
        lastUpdated bindable: false
    }
}
