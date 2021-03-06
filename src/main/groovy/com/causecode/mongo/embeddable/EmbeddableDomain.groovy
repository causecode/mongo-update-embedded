package com.causecode.mongo.embeddable

import grails.validation.Validateable
import grails.util.Holders

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import org.grails.datastore.mapping.model.MappingFactory

/**
 * A trait for all Embeddable domain classes which ensure that validation checks are performed for the embedded
 * instances. This trait adds a dynamic method to all implementing classes using which we can get the actual domain's
 * complete instance without adding the methods to each and every embeddable domain class.
 *
 * @author Nikhil Sharma
 * @since 1.1.0
 */
trait EmbeddableDomain implements Validateable {

    static String resolveParentDomainClass() {
        return this.simpleName.replaceFirst('Em', '')
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
     * This method is required because the embedded instances are updated using a lower level call to MongoDB, hence
     * the data needs to be a map to persist correctly.
     *
     * @param instance Instance of any class to get declared properties as a Map
     * @return A map of declared properties
     */
    @SuppressWarnings(['Instanceof'])
    Map toMap(Object instance = this) {
        /**
         * When the instance is of type Map and we use instance.class to get the `Class` of the instance, it tries to
         * find the key named "class" inside the instance. So to avoid that instance.getClass() is used.
         */
        Map embeddedData = instance.getClass().declaredFields.findAll { Field field ->
            /*
             * Return all non synthetic & non static fields and fields which implement Collection or Map interface.
             *
             * Please note:
             * Checking non synthetic removes fields generated by the compiler that is needed during runtime.
             * Checking non static removes Log4j variable and other static fields as we don't persist static fields.
             * Checking non implementing interface removes validation error field from the map.
             * Only fields implementing Map or Collection interfaces are considered.
             *
             * More details available at https://docs.oracle.com/javase/tutorial/reflect/member/fieldModifiers.html
             *
             * It is also used at {@link com.causecode.mongo.UpdateEmbeddedInstancesService#resolveFieldsForDirtiness}
             *
             * Note: These checks are safe and no other fields are expected as these classes are simple Groovy classes
             * and not Grails Artefacts and hence there are no dynamic or compile time injections for these classes.
             */
            if (!field.synthetic && !Modifier.isStatic(field.modifiers) &&
                    !field.name.contains('beforeValidateHelper')) {
                if (Collection.isAssignableFrom(field.type) || Map.isAssignableFrom(field.type)) {
                    return true
                }

                return !field.type.isInterface()
            }
        }

        // Collect as map from non synthetic & non static fields
        .collectEntries { Field field ->
            def fieldValue = instance[field.name]

            /*
             * We can also check if the field type is not serializable to satisfy
             * the condition but that check will fail if we implement Serializable
             * interface. So using a defined set of simple fields instead of checking Serializable.
             *
             * https://github.com/grails/grails-data-mapping/blob/master/grails-datastore-core/src/main/groovy/org/
             * grails/datastore/mapping/model/MappingFactory.java#L80
             */
            if (fieldValue) {
                if (fieldValue instanceof Enum) {
                    fieldValue = getEnumValue(fieldValue)
                } else {
                    if (!MappingFactory.isSimpleType(fieldValue.getClass().name)) {
                        // Means value is not a simple field, it must be some instance of other class
                        def iterableValue = getValueForCollectionOrMap(field, fieldValue)

                        if (iterableValue) {
                            fieldValue = iterableValue
                        } else {
                            fieldValue = toMap(fieldValue)
                        }
                    }
                }
            }

            [(field.name): fieldValue]
        }

        return embeddedData
    }

    /**
     * This method checks whether the field type is of Collection or Map and based on that it converts it to Map.
     * @param field - {@link Field} instance for the class property
     * @param fieldValue - value of the instance
     *
     * @return Map representation or the same instance (In case of Simple field)
     */
    def getValueForCollectionOrMap(Field field, def fieldValue) {
        // If field is of type Collection
        if (Collection.isAssignableFrom(field.type)) {
            return fieldValue.collect { def innerInstance ->
                return convertComplexType(innerInstance)
            }
        }

        // If field is of type Map
        if (Map.isAssignableFrom(field.type)) {
            Map fieldValueAsMap = [:]

            fieldValue.entrySet().each { Map.Entry entry ->
                fieldValueAsMap[entry.key] = convertComplexType(entry.value)
            }

            return fieldValueAsMap
        }
    }

    /**
     * This method checks whether the value is of type Enum and based on that it returns the value of the Enum.
     * @param value - Value to be checked against
     *
     * @return - Extracted value of the Enum field.
     */
    def getEnumValue(def value) {
        if (value.hasProperty('id')) {
            return value.id
        }

        return value.name()
    }

    /**
     * This method converts the complex object type into Map.
     * @param instance - instance of any class
     *
     * @return - Map representation of the instance property or same instance (In case of Simple type)
     */
    @SuppressWarnings(['Instanceof'])
    def convertComplexType(def instance) {
        if (instance instanceof Enum) {
            return getEnumValue(instance)
        }

        if (!MappingFactory.isSimpleType(instance.getClass().name)) {
            return toMap(instance)
        }

        return instance
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
