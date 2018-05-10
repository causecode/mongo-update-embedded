package test

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.bson.types.ObjectId

/**
 * A test class to test resolveParentDomainClass method in Embeddable domain trait.
 */
@EqualsAndHashCode
@ToString(includes = ['id'], includePackage = false)
class TestEmailDomain {

    ObjectId id
    String email

    Date dateCreated
    Date lastUpdated

    static embedded = []

    static constraints = {
        dateCreated bindable: false
        lastUpdated bindable: false
    }

    EmTestEmailDomain getEmbeddedInstance() {
        return new EmTestEmailDomain(this.id, this.email)
    }
}
