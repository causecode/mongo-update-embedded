/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded

import grails.util.Holders
import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

/**
 * A generic listener used to listen pre update event i.e. {@link #onPersistenceEvent onPersistenceEvent}
 * is called just before the domain is being updated and persisted to the database.
 */
@Slf4j
class PreUpdateEventListener extends AbstractPersistenceEventListener {

    UpdateEmbeddedInstancesService updateEmbeddedInstancesService

    PreUpdateEventListener(final Datastore datastore) {
        super(datastore)
        updateEmbeddedInstancesService = Holders.applicationContext.getBean('updateEmbeddedInstancesService')
    }

    /*
     * Method which is called when a domain instance is updated.
     */
    @Override
    @Synchronized
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        log.debug "PreUpdateEventListener invoked for ${event.eventType}"

        // Instance of domain which is being updated.
        Object domainInstance = event.entityObject
        String domainClassName = domainInstance.class.simpleName

        List fieldsToCheckForDirty = updateEmbeddedInstancesService.getFieldsToCheckForDirty(domainClassName)

        if (!fieldsToCheckForDirty) {
            log.debug "No fields found to check dirty for domain $domainClassName"
            return
        }

        boolean isDirty
        // Check each marked property if that has been updated
        for (fieldName in fieldsToCheckForDirty) {
            if (domainInstance.isDirty(fieldName)) {
                isDirty = true
                break
            }
        }

        if (isDirty) {
            updateEmbeddedInstancesService.addToUpdateQueue(domainInstance)
        } else {
            log.debug "Nothing to add to queue for update in domain $domainClassName"
        }
    }

    @Override
    boolean supportsEventType(Class eventType) {
        // Only listen to the PreUpdate event.
        return (eventType == PreUpdateEvent)
    }
}
