/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded.embeddable

import grails.validation.Validateable
import grails.util.Holders

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import org.grails.datastore.mapping.model.MappingFactory

/**
 * A trait for all Embeddable domain classes which ensure that validation checks are performed for the embedded
 * instances. This trait adds a dynamic method to all implementing classes using which we can get the actual domain's
 * complete instance without adding the methods to each and every embeddedable domain class.
 *
 * @author Nikhil Sharma
 * @since 1.1.0
 */
trait EmbeddableDomain implements Validateable {

    static String resolveParentDomainClass() {
        return this.simpleName.replace('Em', '')
    }

    def methodMissing(String methodName, args) {
        if (methodName == 'getActualInstance') {
            String domainName = resolveParentDomainClass()
            Class domainClass = Holders.grailsApplication.domainClasses.find { it.clazz.simpleName == domainName }.clazz
            return domainClass?.get(this.instanceId)
        }

        throw new MissingMethodException(methodName, this.class, args)
    }

    @SuppressWarnings(['UnnecessaryGetter'])
    def propertyMissing(String propertyName) {
        if (propertyName == 'actualInstance') {
            return this.getActualInstance()
        }

        throw new MissingPropertyException(propertyName, this.class)
    }

    /**
     * Return a map of declared properties defined in the given class instance using Java reflection.
     *
     * @param instance Instance of any class to get declared properties as a Map
     * @param useEnumID Whether to convert enums as enum String name or to use the enum ID
     * @return A map of declared properties
     */
    @SuppressWarnings(['Instanceof'])
    Map toMap(Object instance = this) {
        Map embeddedData = instance.class.declaredFields.findAll { Field field ->
            /*
             * Return all non synthetic & non static fields and must not implement an interface.
             *
             * Please note:
             * Checking non synthetic removes time stamp & other grails utility fields from the map.
             * Checking non static removes Log4j variable.
             * Checking non implementing interface removes validation error field from the map.
             */
            !field.synthetic && !Modifier.isStatic(field.modifiers) && !field.type.isInterface() &&
                    !field.name.contains('beforeValidateHelper')
        }
        // Collect as map from non synthetic & non static fields
        .collectEntries { Field field ->
            def value = instance[field.name]

            /*
             * We can also check if the field type is not serializable to satisfy
             * the condition but that check will fail if we implement Serializable
             * interface. So using a defined set of simple fields instead of checking Serializable.
             *
             * https://github.com/grails/grails-data-mapping/blob/master/grails-datastore-core/src/main/groovy/org/
             * grails/datastore/mapping/model/MappingFactory.java#L80
             */
            if (value) {
                if (value instanceof Enum) {
                    if (value.hasProperty('id')) {
                        value = value.id
                    } else {
                        value = value.name()
                    }
                } else {
                    if (!MappingFactory.isSimpleType(value.getClass().name)) {
                        // Means value is not a simple field, it must be some instance of other class
                        value = toMap(value)
                    }
                }
            }

            [(field.name): value]
        }

        return embeddedData
    }

    @Override
    boolean equals(Object obj) {
        if (obj == null) {
            return false
        }

        if (getClass() != obj.getClass()) {
            return false
        }

        EmbeddableDomain newObject = (EmbeddableDomain) obj
        return instanceId == newObject.instanceId
    }

    @Override
    int hashCode() {
        return instanceId.hashCode()
    }
}
