/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo

import com.mongodb.client.result.UpdateResult
import grails.core.GrailsApplication
import groovy.transform.Synchronized
import org.grails.datastore.mapping.mongo.MongoConstants

/**
 * This service is used for creating instances of the EmbeddedInstanceQueue and then processing the queue via a call
 * from the UpdateEmbeddedInstancesJob.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
@SuppressWarnings(['GrailsStatelessService'])
class EmbeddedInstanceQueueService {

    GrailsApplication grailsApplication

    static final Map FLUSH_TRUE = [flush: true]
    static final String STATUS = 'status'
    static final Integer THREE = 3

    /**
     * This method creates the instances of EmbeddedInstanceQueue and is triggered from the PreUpdateEventListener
     * whenever there is an update in a domain instance.
     *
     * @param domainToUpdate Name of the domain that is to be updated.
     * @param fieldInfo A Map holding field's information like name and whether it is an array.
     * @param sourceDomainInstance Instance of the domain whose update triggered the PreUpdateEventListener.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    @Synchronized
    void addToQueue(String domainToUpdate, Map fieldInfo, Object sourceDomainInstance) {
        String sourceDomain = 'sourceDomain'
        String sourceDomainId = 'sourceDomainId'
        String domainToBeUpdated = 'domainToUpdate'
        String fieldToBeUpdated = 'fieldToUpdate'

        Map propertiesMap = [(domainToBeUpdated): domainToUpdate, (fieldToBeUpdated): fieldInfo.fieldName,
                isFieldArray: fieldInfo.isFieldArray]
        propertiesMap[sourceDomain] = sourceDomainInstance.getClass().simpleName
        propertiesMap[sourceDomainId] = sourceDomainInstance.id

        log.info "Property map for EmbeddedQueueInstance $propertiesMap"

        EmbeddedInstanceQueue embeddedInstanceQueue = EmbeddedInstanceQueue.withCriteria {
            eq(sourceDomain, propertiesMap[sourceDomain])
            eq(sourceDomainId, propertiesMap[sourceDomainId])
            eq(fieldToBeUpdated, propertiesMap[fieldToBeUpdated])
            eq(domainToBeUpdated, propertiesMap[domainToBeUpdated])
            eq(STATUS, EmbeddedInstanceQueueStatus.ACTIVE)

            maxResults(1)
        } [0]

        if (embeddedInstanceQueue) {
            log.debug "Found active record for [$domainToUpdate] from [${propertiesMap[sourceDomain]}]"
            return
        }

        embeddedInstanceQueue = new EmbeddedInstanceQueue(propertiesMap)
        if (!embeddedInstanceQueue.save(FLUSH_TRUE)) {
            log.error "Failed to create EmbeddedInstanceQueue instance due to ${embeddedInstanceQueue.errors}"
        }
    }

    /**
     * This method returns the required class corresponding to a domain.
     *
     * @param className The name of the domain.
     * @return Class The class representing this domain.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    Class getDomainClass(String className) {
        return grailsApplication.domainClasses.find { it.name == className }?.clazz
    }

    /**
     * This method is used to process the EmbeddedInstanceQueue's instances. The method iterates over all the
     * instances marked as active and then updates the embedded instances, and finally marks those instances processed.
     * This is triggered by the UpdateEmbeddedInstancesJob every 2 hours.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    @SuppressWarnings(['CatchException'])
    @Synchronized
    void processEmbeddedInstanceQueue() {
        List embeddedInstanceQueueList = EmbeddedInstanceQueue.withCriteria {
            eq(STATUS, EmbeddedInstanceQueueStatus.ACTIVE)
            le('attemptCount', THREE)

            maxResults(1000)
        }

        int count = embeddedInstanceQueueList.size()

        if (count < 1) {
            return
        }

        log.debug "Found [${count}] records to update embedded instances"

        embeddedInstanceQueueList.each { EmbeddedInstanceQueue embeddedInstanceQueueInstance ->
            log.info ("Updating instances of domain [${embeddedInstanceQueueInstance.domainToUpdate}] due to update " +
                    "in ${embeddedInstanceQueueInstance.sourceDomain}")

            Class sourceClass = getDomainClass(embeddedInstanceQueueInstance.sourceDomain)
            Class classToUpdate = getDomainClass(embeddedInstanceQueueInstance.domainToUpdate)

            try {
                Object sourceDomainInstance = sourceClass.get(embeddedInstanceQueueInstance.sourceDomainId)
                String fieldToUpdate = embeddedInstanceQueueInstance.fieldToUpdate
                String fieldToMatch = fieldToUpdate + '.instanceId'

                if (embeddedInstanceQueueInstance.isFieldArray) {
                    fieldToUpdate = fieldToUpdate + '.\$'
                }

                Map embeddedMap = sourceDomainInstance.embeddedInstance.toMap()

                boolean hasLastUpdatedField = classToUpdate.declaredFields?.name?.contains('lastUpdated')

                Map query = [(fieldToMatch): sourceDomainInstance.id]
                Map updateOperation = [(MongoConstants.SET_OPERATOR): hasLastUpdatedField ?
                        [(fieldToUpdate): embeddedMap, 'lastUpdated': new Date()] : [(fieldToUpdate): embeddedMap]]

                log.debug "Match query: $query, Update operation: $updateOperation and update data: $embeddedMap"

                Map options = [upsert: false]
                UpdateResult result = classToUpdate.collection.updateMany(query, updateOperation, options)

                log.debug "Update query complete result: [$result]"

                embeddedInstanceQueueInstance.status = EmbeddedInstanceQueueStatus.PROCESSED
            } catch (Exception e) {
                log.error "Error updating queued embedded instance [$embeddedInstanceQueueInstance]", e

                if (embeddedInstanceQueueInstance.attemptCount == THREE) {
                    log.debug "3 attempts has been done to update $embeddedInstanceQueueInstance, setting status failed"
                    embeddedInstanceQueueInstance.status = EmbeddedInstanceQueueStatus.FAILED
                    embeddedInstanceQueueInstance.save(FLUSH_TRUE)
                }
            } finally {
                embeddedInstanceQueueInstance.attemptCount++
                embeddedInstanceQueueInstance.save(FLUSH_TRUE)
            }
        }
    }
}
