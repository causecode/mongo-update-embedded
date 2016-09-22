/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.mongoupdateembedded

import grails.plugins.Plugin
import org.grails.datastore.mapping.core.Datastore

class MongoUpdateEmbeddedGrailsPlugin extends Plugin {

    // The version or versions of Grails the plugin is designed for
    def grailsVersion = "3.2.0.RC1 > *"

    // Resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Mongo Update Embedded" // Headline display name of the plugin
    def author = "CauseCode Technologies"
    def authorEmail = "vishesh@causecode.com"
    def description = '''\
            This plugin is used to update embedded isntances of a domain class that has been embedded using a class with
            some fields of the domain instead of embedding the entire domain.
            '''

    def developers = [[name: "Vishesh Duggar", email: "vishesh@causecode.com"],
            [name: "Nikhil Sharma", email: "nikhil.sharma@causecode.com"]]

    /*
     * Note: Few default methods that were not required were removed. Please refer plugin docs if required.
     * Removed methods: doWithSpring, doWithDynamicMethods, onChange, onConfigChange and onShutdown.
     */

    void doWithApplicationContext() {
        def mainContext = grailsApplication.mainContext

        UpdateEmbeddedInstancesService updateEmbeddedInstancesService = mainContext.getBean('updateEmbeddedInstancesService')
        updateEmbeddedInstancesService.createEmbeddedDomainsMap()

        mainContext.getBeansOfType(Datastore).values().each { Datastore d ->
            mainContext.addApplicationListener(new PreUpdateEventListener(d))
        }
    }
}
