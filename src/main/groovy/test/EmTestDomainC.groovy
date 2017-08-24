package test

import com.causecode.mongo.embeddable.EmbeddableDomain
import org.bson.types.ObjectId

/**
 * This class represents an embedded instance of test domain class {@link TestDomainC}.
 *
 * @author Hardik Modha
 * @since 0.0.7
 */
class EmTestDomainC implements EmbeddableDomain {

    ObjectId instanceId

    String name

    List<String> collectionTypeListOfString

    Set<String> collectionTypeSetOfString

    List<Integer> collectionTypeListOfInteger

    Set<Integer> collectionTypeSetOfInteger

    List<Car> collectionTypeListOfEnum

    List<EmTestDomainA> collectionTypeListOfObjects

    Map<String, String> mapOfString

    EmTestDomainC() {

    }

    static embedded = ['collectionTypeListOfObjects']

    EmTestDomainC(Map properties) {
        this.instanceId = properties.instanceId
        this.name = properties.name
        this.collectionTypeListOfString = properties.collectionTypeListOfString
        this.collectionTypeSetOfString = properties.collectionTypeSetOfString
        this.collectionTypeListOfInteger = properties.collectionTypeListOfInteger
        this.collectionTypeSetOfInteger = properties.collectionTypeSetOfInteger
        this.collectionTypeListOfEnum = properties.collectionTypeListOfEnum
        this.collectionTypeListOfObjects = properties.collectionTypeListOfObjects
        this.mapOfString = properties.mapOfString
    }
}
