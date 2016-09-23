/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package test

import com.causecode.mongoupdateembedded.embeddable.EmbeddableDomain

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
