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
 * A domain class without timestamp fields to verify update functionality even when timestamp fields are missing.
 *
 * @author Ankit Agrawal
 * @since 1.0.0
 */
@SuppressWarnings(['GrailsDomainTimestampFields'])
@EqualsAndHashCode
@ToString(includes = ['id'], includePackage = false)
class TestDomainE {

    ObjectId id

    EmTestEmailDomain emTestEmailDomain

    static embedded = ['emTestEmailDomain']

    static constraints = {
    }
}
