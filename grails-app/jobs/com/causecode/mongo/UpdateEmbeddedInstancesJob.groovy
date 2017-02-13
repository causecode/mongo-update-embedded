/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo

/**
 * This Job is used for processing the pending embedded instances queue. This job starts with a delay of 2 minutes
 * and repeats every 2 minutes.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
class UpdateEmbeddedInstancesJob {

    EmbeddedInstanceQueueService embeddedInstanceQueueService

    static final long TWO_MINUTES = 120000

    static triggers = {
        simple startDelay: TWO_MINUTES, repeatInterval: TWO_MINUTES
    }

    def execute() {
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()
    }
}
