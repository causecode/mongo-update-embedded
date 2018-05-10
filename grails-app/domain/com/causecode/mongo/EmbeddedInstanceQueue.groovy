package com.causecode.mongo

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

/**
 * A domain that holds instances for updating the embedded instances of any domain class. It is used as a queue which
 * is processed in regular intervals by a job.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
@EqualsAndHashCode
class EmbeddedInstanceQueue {

    ObjectId id

    ObjectId sourceDomainId
    String sourceDomain

    String domainToUpdate
    String fieldToUpdate

    // This field indicates whether to insert the MongoDB's `.$` operator for updating within arrays.
    boolean isFieldArray

    EmbeddedInstanceQueueStatus status = EmbeddedInstanceQueueStatus.ACTIVE

    Date dateCreated
    Date lastUpdated

    int attemptCount

    static embedded = []

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

@SuppressWarnings(['GrailsDomainHasEquals'])
enum EmbeddedInstanceQueueStatus {
    ACTIVE(1),
    PROCESSED(2),
    FAILED(3)

    final int id
    EmbeddedInstanceQueueStatus(int id) {
        this.id = id
    }

    @Override
    String toString() {
        return "EmbeddedInstanceQueueStatus (${this.name()})($id)"
    }
}
