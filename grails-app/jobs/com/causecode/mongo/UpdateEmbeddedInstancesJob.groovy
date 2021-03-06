package com.causecode.mongo

import grails.util.Holders

/**
 * This Job is used for processing the pending embedded instances queue. This job starts with overridden startDelay and
 * repeatInterval provided by installing app otherwise with a default delay of 2 minutes and repeats every 2 minutes.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
class UpdateEmbeddedInstancesJob {

    EmbeddedInstanceQueueService embeddedInstanceQueueService

    static final long TWO_MINUTES = 120000

    static triggers = {
        simple startDelay: Holders.config.jobs.mongo.update.embedded.startDelay ?: TWO_MINUTES,
        repeatInterval: Holders.config.jobs.mongo.update.embedded.repeatInterval ?: TWO_MINUTES
    }

    def execute() {
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()
    }
}
