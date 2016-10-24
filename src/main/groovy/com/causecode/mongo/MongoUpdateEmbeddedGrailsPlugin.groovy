/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongo

import grails.plugins.Plugin
import org.grails.datastore.mapping.core.Datastore

/**
 * This class is used as the descriptor for this plugin.
 *
 * @author CauseCode Technologies
 */
class MongoUpdateEmbeddedGrailsPlugin extends Plugin {

    // The version or versions of Grails the plugin is designed for
    def grailsVersion = '3.2.0.RC1 > *'

    // Resources that are excluded from plugin packaging
    def pluginExcludes = [
        '**/test/**'
    ]

    // TODO Fill in these fields
    def title = 'Mongo Update Embedded' // Headline display name of the plugin
    def author = 'CauseCode Technologies'
    def authorEmail = 'vishesh@causecode.com'
    def description = '''\
            This plugin helps trickle down updates to a domain instance to its embedded instances in other domain
            classes. Read more on embedded objects <http://docs.grails.org/latest/ref/Domain%20Classes/embedded.html>.
            '''

    def developers = [[name: 'Vishesh Duggar', email: authorEmail],
            [name: 'Nikhil Sharma', email: 'nikhil.sharma@causecode.com']]

    /*
     * Note: Few default methods that were not required were removed. Please refer plugin docs if required.
     * Removed methods: doWithSpring, doWithDynamicMethods, onChange, onConfigChange and onShutdown.
     */

    void doWithApplicationContext() {
        def mainContext = grailsApplication.mainContext

        mainContext.getBean('updateEmbeddedInstancesService').embeddedDomainsMap

        // Registering the PreUpdateEvent listener.
        mainContext.getBeansOfType(Datastore).values().each { Datastore d ->
            mainContext.addApplicationListener(new PreUpdateEventListener(d))
        }
    }
}
