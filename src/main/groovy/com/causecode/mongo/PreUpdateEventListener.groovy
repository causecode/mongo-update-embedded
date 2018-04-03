package com.causecode.mongo

import grails.util.Holders
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

/**
 * A Generic Listener for Gorm's PreUpdateEvent that is called just before the domain is being updated and persisted to
 * the database.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
@Slf4j
class PreUpdateEventListener extends AbstractPersistenceEventListener {

    UpdateEmbeddedInstancesService updateEmbeddedInstancesService

    PreUpdateEventListener(final Datastore datastore) {
        super(datastore)
        updateEmbeddedInstancesService = Holders.applicationContext.getBean('updateEmbeddedInstancesService')
    }

    /**
     * This method is invoked when a domain instance is updated. The instance to be updated is matched for fields
     * to be dirty that is present in the domain's embeddable class. If any of the field that is present in the
     * embeddable class is dirty, then the EmbeddedQueueInstance's instances are created.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    @Override
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
            updateEmbeddedInstancesService.enqueue(domainInstance)
        } else {
            log.debug "Nothing to add to queue for update in domain $domainClassName"
        }
    }

    /**
     * This method checks the event type and returns true only if the event type is PreUpdateEvent which invokes the
     * onPersistenceEvent listener.
     *
     * @param eventType The class representing the type of event.
     * @return boolean true If the event type is PreUpdateEvent.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    @Override
    boolean supportsEventType(Class eventType) {
        // Only listen to the PreUpdate event.
        return (eventType == PreUpdateEvent)
    }
}
