/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
import com.causecode.mongoupdateembedded.EmbeddedInstanceQueueService

/**
 * This Job is used for processing the pending embedded instances queue. This job starts with a delay of 5 minutes
 * and repeats every 2 hours.
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
        log.debug 'Started executing UpdateEmbeddedInstanceJob Job..'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()
        log.debug 'Finished executing UpdateEmbeddedInstanceJob Job..'
    }
}
