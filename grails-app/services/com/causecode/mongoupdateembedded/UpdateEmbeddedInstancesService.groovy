/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded

import grails.core.GrailsApplication
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import org.grails.core.DefaultGrailsDomainClass

class UpdateEmbeddedInstancesService {

    static Map domainsThatEmbed = [:]
    static Map embeddedClassFields = [:]

    GrailsApplication grailsApplication
    EmbeddedInstanceQueueService embeddedInstanceQueueService

    void createEmbeddedDomainsMap() {
        // Iterating all domain classes.
        grailsApplication.domainClasses.each { DefaultGrailsDomainClass domainClass ->

            // Iterating all embedded fields.
            domainClass.embedded.each { String fieldName ->

                Field field = domainClass.clazz.getDeclaredField(fieldName)
                Map fieldInfoMap = [fieldName: fieldName, isFieldArray: false]

                Class clazz
                Type genericType = field.genericType

                if (genericType && (genericType instanceof ParameterizedType)) {
                    clazz = genericType.getActualTypeArguments()[0]
                    fieldInfoMap.isFieldArray = true
                } else {
                    clazz = field.type
                }

                String domainName

                try {
                    domainName = clazz.resolveParentDomainClass()
                } catch (MissingMethodException e) {
                    log.debug "${clazz.simpleName} is not a Domain class."
                    return
                }

                Map domainInfoMap = [(domainClass.name): fieldInfoMap]

                if (domainsThatEmbed[domainName]) {
                    domainsThatEmbed[domainName].putAll(domainInfoMap)
                } else {
                    domainsThatEmbed.put(domainName, domainInfoMap)
                    embeddedClassFields.put(domainName, [fieldList: resolvePrivateFieldNames(clazz)])
                }
            }
        }

        log.info "Embedded domains map: $domainsThatEmbed"
        log.info "Field list for each domain to check dirty for: $embeddedClassFields"
    }

    List<String> resolvePrivateFieldNames(Class clazz) {
        return clazz.declaredFields.findAll { Field field ->
            !field.synthetic && !Modifier.isStatic(field.modifiers) && !field.type.isInterface() &&
                    !field.name.contains('beforeValidateHelper') && !field.name.equals('instanceId')
        }*.name
    }

    List<String> getFieldsToCheckForDirty(String domainName) {
        return embeddedClassFields[domainName]?.fieldList
    }

    void addToUpdateQueue(Object domainInstance) {
        log.debug "Adding queues for updating embedded instance of domain $domainInstance"

        String domainName = domainInstance.class.simpleName
        Map embeddingDomains = domainsThatEmbed[domainName]

        embeddingDomains.each { String domainNameThatEmbed, Map fieldInfo ->
            // Do not remove this withNewSession closure. It is required to avoid exceptions due to sessions.
            EmbeddedInstanceQueue.withNewSession {
                embeddedInstanceQueueService.createInstance(domainNameThatEmbed, fieldInfo, domainInstance)
            }
        }
    }
}
