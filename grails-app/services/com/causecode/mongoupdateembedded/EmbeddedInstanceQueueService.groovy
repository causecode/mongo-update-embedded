/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded

import com.mongodb.client.result.UpdateResult
import grails.core.GrailsApplication
import groovy.transform.Synchronized
import org.grails.datastore.mapping.mongo.MongoConstants

class EmbeddedInstanceQueueService {

    GrailsApplication grailsApplication

    static final String SOURCE_DOMAIN = 'sourceDomain'
    static final String SOURCE_DOMAIN_ID = 'sourceDomainId'
    static final String DOMAIN_TO_UPDATE = 'domainToUpdate'
    static final String FIELD_TO_UPDATE = 'fieldToUpdate'

    @Synchronized
    void createInstance(String domainToUpdate, Map fieldInfo, Object sourceDomainInstance) {
        Map propertiesMap = [(DOMAIN_TO_UPDATE): domainToUpdate, (FIELD_TO_UPDATE): fieldInfo.fieldName,
                isFieldArray: fieldInfo.isFieldArray]
        propertiesMap[SOURCE_DOMAIN] = sourceDomainInstance.getClass().simpleName
        propertiesMap[SOURCE_DOMAIN_ID] = sourceDomainInstance.id

        log.info "Property map for EmbeddedQueueInstance $propertiesMap"

        EmbeddedInstanceQueue embeddedInstanceQueue = EmbeddedInstanceQueue.withCriteria {
            eq(SOURCE_DOMAIN, propertiesMap[SOURCE_DOMAIN])
            eq(SOURCE_DOMAIN_ID, propertiesMap[SOURCE_DOMAIN_ID])
            eq(FIELD_TO_UPDATE, propertiesMap[FIELD_TO_UPDATE])
            eq(DOMAIN_TO_UPDATE, propertiesMap[DOMAIN_TO_UPDATE])
            eq("status", EmbeddedInstanceQueueStatus.ACTIVE)

            maxResults(1)
        }

        if (embeddedInstanceQueue && embeddedInstanceQueue.id) {
            log.debug "Found active record for [$DOMAIN_TO_UPDATE] from [$SOURCE_DOMAIN]"
            return
        }

        embeddedInstanceQueue = new EmbeddedInstanceQueue(propertiesMap)
        if (!embeddedInstanceQueue.save(flush: true)) {
            log.error 'Failed to create EmbeddedInstanceQueue instance.'
        }
    }

    Class getDomainClass(String className) {
        return grailsApplication.domainClasses.find {it.name == className}.clazz
    }

    @Synchronized
    void processEmbeddedInstanceQueue() {
        List embeddedInstanceQueueList = EmbeddedInstanceQueue.findAllByStatus(EmbeddedInstanceQueueStatus.ACTIVE)

        log.debug "Found [${embeddedInstanceQueueList.size()}] records to update embedded instances"

        embeddedInstanceQueueList.each { EmbeddedInstanceQueue embeddedInstanceQueueInstance ->
            log.info ("Updating instances of domain [${embeddedInstanceQueueInstance.domainToUpdate}] due to update " +
                    "in ${embeddedInstanceQueueInstance.sourceDomain}")

            Class sourceClass = getDomainClass(embeddedInstanceQueueInstance.sourceDomain)
            Class classToUpdate = getDomainClass(embeddedInstanceQueueInstance.domainToUpdate)

            try {
                Object domainInstance = sourceClass.get(embeddedInstanceQueueInstance.sourceDomainId)
                String fieldToUpdate = embeddedInstanceQueueInstance.fieldToUpdate
                String fieldToMatch = fieldToUpdate + '.instanceId'

                Map options = [multi: true, upsert: false]

                if (embeddedInstanceQueueInstance.isFieldArray) {
                    fieldToUpdate = fieldToUpdate + '.\$'
                }

                Map embeddedMap = domainInstance.embeddedInstance.toMap()

                Map query = [(fieldToMatch): domainInstance.id]
                Map updateOperation = [(MongoConstants.SET_OPERATOR): [(fieldToUpdate): embeddedMap]]

                log.debug "Match query: $query, Update operation: $updateOperation and update data: $embeddedMap"

                UpdateResult result = classToUpdate.collection.update(query, updateOperation, options)

                log.debug "Update query complete result: [$result]"

                embeddedInstanceQueueInstance.status = EmbeddedInstanceQueueStatus.PROCESSED
                embeddedInstanceQueueInstance.save(flush: true)
            } catch (Exception e) {
                log.error "Error updating queued embedded instance [$embeddedInstanceQueueInstance]", e
            }
        }
    }
}
