package test

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.bson.types.ObjectId

/**
 * A test domain class for testing mongo-update-embedded update operation against various Collection and Map types.
 *
 * @author Hardik Modha
 * @since 0.0.7
 */
@EqualsAndHashCode
@ToString(includes = ['id'], includePackage = false)
class TestDomainC {
    ObjectId id

    String name

    List<String> collectionTypeListOfString = []

    Set<String> collectionTypeSetOfString = [] as Set

    List<Integer> collectionTypeListOfInteger = []

    Set<Integer> collectionTypeSetOfInteger = [] as Set

    List<Car> collectionTypeListOfEnum = []

    List<EmTestDomainA> collectionTypeListOfObjects = []

    Map<String, String> mapOfString = [:]

    Date dateCreated
    Date lastUpdated

    static embedded = ['collectionTypeListOfObjects']

    static constraints = {
        dateCreated bindable: false
        lastUpdated bindable: false
    }

    EmTestDomainC getEmbeddedInstance() {
        return new EmTestDomainC([
                instanceId: this.id,
                name: this.name,
                collectionTypeListOfString: this.collectionTypeListOfString,
                collectionTypeSetOfString: this.collectionTypeSetOfString,
                collectionTypeListOfInteger: this.collectionTypeListOfInteger,
                collectionTypeSetOfInteger: this.collectionTypeSetOfInteger,
                collectionTypeListOfEnum: this.collectionTypeListOfEnum,
                collectionTypeListOfObjects: this.collectionTypeListOfObjects,
                mapOfString: this.mapOfString,
        ])
    }
}

/**
 * An Enum to use for testing.
 */
@SuppressWarnings(['GrailsDomainHasEquals'])
enum Car {
    BUGATTI('Bugatti'),
    MASERATI('Maserati'),
    KOENIGSEGG('Koenigsegg')

    final String name
    Car(String name) {
        this.name = name
    }

    @Override
    String toString() {
        return "Car (${this.name()})($name)"
    }
}
