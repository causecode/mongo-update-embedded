package test

import com.causecode.mongo.embeddable.EmbeddableDomain
import org.bson.types.ObjectId

/**
 * A test class to test resolveParentDomainClass method in Embeddable domain trait.
 */
class EmTestEmailDomain implements EmbeddableDomain {

    ObjectId instanceId
    String email

    EmTestEmailDomain() {
    }

    EmTestEmailDomain(ObjectId instanceId, String email) {
        this.instanceId = instanceId
        this.email = email
    }
}
