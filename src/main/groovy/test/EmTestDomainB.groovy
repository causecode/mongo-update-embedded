package test

import com.causecode.mongo.embeddable.EmbeddableDomain

/**
 * A test class for representing embeddable class for test cases. This will be excluded from plugin while packaging.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
class EmTestDomainB implements EmbeddableDomain {

    String testB
    EmTestDomainA testDomainA
}
