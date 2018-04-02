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
class TestDomainA {

    ObjectId id

    String testField1
    String testField2

    ObjectId embeddingNonDomainField

    Status status = Status.ONE

    Date dateCreated
    Date lastUpdated

    EmTestDomainA getEmbeddedInstance() {
        return new EmTestDomainA(this.id, this.testField1, this.status)
    }

    static embedded = ['embeddingNonDomainField']

    static constraints = {
        dateCreated bindable: false
        lastUpdated bindable: false
    }

    @Override
    String toString() {
        return "TestDomainA ($id)"
    }
}

@SuppressWarnings(['GrailsDomainHasEquals'])
enum Status {
    ONE(1)

    final int id
    Status(int id) {
        this.id = id
    }

    @Override
    String toString() {
        return "Status (${this.name()})($id)"
    }
}
