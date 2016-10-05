/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package test

import com.causecode.mongo.embeddable.EmbeddableDomain
import org.bson.types.ObjectId

/**
 * A test class for representing embeddable class for test cases. This will be excluded from plugin while packaging.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
class EmTestDomainA implements EmbeddableDomain {

    ObjectId instanceId
    String testField1
    Status status

    EmTestDomainA() {
    }

    EmTestDomainA(ObjectId instanceId, String testField1, Status status) {
        this.instanceId = instanceId
        this.testField1 = testField1
        this.status = status
    }
}
