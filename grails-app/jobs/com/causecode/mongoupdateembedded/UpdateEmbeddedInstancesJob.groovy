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

    static final long FIVE_MINUTES = 300000
    static final long TWO_HOURS = 7200000

    static triggers = {
        simple repeatInterval: TWO_HOURS, startDelay: FIVE_MINUTES
    }

    def execute() {
        log.debug 'Started executing UpdateEmbeddedInstanceJob Job..'
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()
        log.debug 'Finished executing UpdateEmbeddedInstanceJob Job..'
    }
}
