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

    Date dateCreated
    Date lastUpdated

    static embedded = ['testDomainA', 'testDomainASet']

    static constraints = {
        dateCreated bindable: false
        lastUpdated bindable: false
    }

    @Override
    String toString() {
        return "TestDomainB ($id)"
    }
}
