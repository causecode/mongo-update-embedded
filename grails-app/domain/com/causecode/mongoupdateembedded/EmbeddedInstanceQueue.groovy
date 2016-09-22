/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

@EqualsAndHashCode
class EmbeddedInstanceQueue {

    ObjectId id

    ObjectId sourceDomainId
    String sourceDomain

    String domainToUpdate
    String fieldToUpdate
    boolean isFieldArray

    EmbeddedInstanceQueueStatus status = EmbeddedInstanceQueueStatus.ACTIVE

    static constraints = {
    }

    static mapping = {
        sourceDomainId index: true
        domainToUpdate index: true
        fieldToUpdate index: true
        sourceDomain index: true
        status index: true
    }

    @Override
    String toString() {
        return "EmbeddedInstanceQueue to update domain $domainToUpdate for update in $sourceDomain"
    }
}

enum EmbeddedInstanceQueueStatus {
    ACTIVE(1),
    PROCESSED(2)

    final int id
    EmbeddedInstanceQueueStatus(int id) {
        this.id = id
    }

    @Override
    String toString() {
        return "EmbeddedInstanceQueueStatus (${this.name()})($id)"
    }
}
