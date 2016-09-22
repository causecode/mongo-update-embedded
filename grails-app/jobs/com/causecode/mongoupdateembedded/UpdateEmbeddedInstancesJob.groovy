/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
import com.causecode.mongoupdateembedded.EmbeddedInstanceQueueService

class UpdateEmbeddedInstancesJob {

    EmbeddedInstanceQueueService embeddedInstanceQueueService

    static triggers = {
        simple repeatInterval: 10000, startDelay: 2000
    }

    def execute() {
        log.debug "Started executing UpdateEmbeddedInstanceJob Job.."
        embeddedInstanceQueueService.processEmbeddedInstanceQueue()
        log.debug "Finished executing UpdateEmbeddedInstanceJob Job.."
    }
}
