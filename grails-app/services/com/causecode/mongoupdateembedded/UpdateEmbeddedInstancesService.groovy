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

/**
 * This is the base service of this plugin which is responsible for creating the in memory maps, embedded instance
 * queue and field list to be matched when a domain is updated.
 *
 * @author Nikhil Sharma
 * @since 0.0.1
 */
@SuppressWarnings(['GrailsStatelessService'])
class UpdateEmbeddedInstancesService {

    static Map domainsThatEmbed = [:]
    static Map embeddedClassFields = [:]

    GrailsApplication grailsApplication
    EmbeddedInstanceQueueService embeddedInstanceQueueService

    /**
     * This method initializes the maps required for creating the queue instances for updating the embedded instances.
     * It iterates over all domain classes, for each domain class, iterates over the static embedded field, for each
     * embedded field name, gets the field class (also checks if field is array type), gets the respective domain
     * class name and then add the domain in iteration to this domain's 'domainsThatEmbed' map.
     *
     * Example:
     * Consider a domain A that embeds domain B with field name b. While iterating over A, the map will be created for B
     * as:
     *
     * domainsThatEmbed: [B: [A: b]]
     *
     * which states the domain B is embedded in domain A having field name b.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    @SuppressWarnings(['Instanceof'])
    void initializeEmbeddedDomainsMap() {
        // Iterating all domain classes.
        grailsApplication.domainClasses.each { DefaultGrailsDomainClass domainClass ->

            String currentDomainClassName = domainClass.name
            Map domainInfoMap = [(currentDomainClassName): []]

            // Iterating all embedded fields.
            domainClass.embedded.each { String fieldName ->

                Field field = domainClass.clazz.getDeclaredField(fieldName)
                Map fieldInfoMap = [fieldName: fieldName, isFieldArray: false]

                Class clazz
                Type genericType = field.genericType

                if (genericType && (genericType instanceof ParameterizedType)) {
                    clazz = genericType.actualTypeArguments[0]
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

                domainInfoMap[currentDomainClassName].add(fieldInfoMap)

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

    /**
     * This method is used to get all fields present in the Embedded class of any domain excluding the fields that
     * are not required to be matched for any changes.
     *
     * @param clazz The Embedded class.
     * @return List List of field names.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    List<String> resolvePrivateFieldNames(Class clazz) {
        return clazz?.declaredFields.findAll { Field field ->
            !field.synthetic && !Modifier.isStatic(field.modifiers) && !field.type.isInterface() &&
                    !field.name.contains('beforeValidateHelper') && (!(field.name == 'instanceId'))
        }*.name
    }

    /**
     * This method returns the field names of the Embedded class from the static map that holds fields for every
     * domain that needs to be matched for changes in the domain instance being updated.
     *
     * @param domainName The name of the domain. It acts as the key in the map.
     * @return List List of field names.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    List<String> getFieldsToCheckForDirty(String domainName) {
        return embeddedClassFields[domainName]?.fieldList
    }

    /**
     * This method is used to add to the queue the domain instances that embeds the domain that is currently being
     * updated.
     *
     * @param domainInstance Object Instance of the domain currently being updated.
     *
     * @author Nikhil Sharma
     * @since 0.0.1
     */
    void addToUpdateQueue(Object domainInstance) {
        log.debug "Adding queues for updating embedded instance of domain $domainInstance"

        String domainName = domainInstance.class.simpleName
        Map embeddingDomains = domainsThatEmbed[domainName]

        embeddingDomains.each { String domainNameThatEmbed, List fields ->
            fields.each { Map fieldInfo ->
                // Do not remove this withNewSession closure. It is required to avoid exceptions due to sessions.
                EmbeddedInstanceQueue.withNewSession {
                    embeddedInstanceQueueService.addToQueue(domainNameThatEmbed, fieldInfo, domainInstance)
                }
            }
        }
    }
}
