/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.quality.configurations.codenarc

import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.grails.GrailsUtil
import org.codehaus.groovy.ast.ClassNode

/**
 * This is a custom codenarc rule that checks that all domains should have timestamp fields
 *
 * @author Ankit Agrawal
 * @since 0.0.9
 */
class GrailsDomainTimestampFieldsRule extends AbstractAstVisitorRule {
    String name = 'GrailsDomainTimestampFields'
    String description = 'All domain classes should have timestamp fields.'
    int priority = 1
    Class astVisitorClass = GrailsDomainTimestampFieldsRuleAstVisitor
    String applyToFilesMatching = GrailsUtil.DOMAIN_FILES
}

class GrailsDomainTimestampFieldsRuleAstVisitor extends AbstractAstVisitor {

    @Override
    void visitClassComplete(ClassNode classNode) {
        if (!classNode.enum && !classNode.fields.name.containsAll(['dateCreated', 'lastUpdated'])) {
            addViolation(classNode, "The domain class $classNode.name should contain timestamp fields " +
                    "dateCreated and lastUpdated")
        }
    }
}
