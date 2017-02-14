/*
 * Copyright 2015 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package com.intershop.gradle.jiraconnector.util

import com.atlassian.jira.rest.client.api.domain.Field
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * This class contains all information about a JIRA field
 */
@CompileStatic
@Slf4j
class JiraField {

    final static String FIXVERSIONS = 'fixVersions'
    final static String LABELS = 'labels'
    final static String VERSIONS = 'versions'

    final static String[] SUPPORTSYSTEMFIELDIDS = [FIXVERSIONS, LABELS, VERSIONS]

    boolean isSystem = false
    boolean isString = false
    boolean isVersion = false
    boolean isArray = false

    final String id
    final String name
    final String system

    JiraField(Field field) {
        id = field.id
        name = field.name

        log.info('JIRA Field is {}.', field.name)

        if(field.getSchema()) {
            isSystem = SUPPORTSYSTEMFIELDIDS.contains(field.getSchema().getSystem())
            if(isSystem) {
                system = field.getSchema().getSystem()
            } else {
                system = null
                isArray = (field.getSchema().getType() == 'array')

                if(isArray) {
                    isString = (field.getSchema().getItems() == 'string')
                    isVersion = (field.getSchema().getItems() == 'version')
                } else {
                    isString = (field.getSchema().getType() == 'string')
                    isVersion = (field.getSchema().getType() == 'version')
                }
            }
        }
    }

    boolean isSupported() {
        return (isString || isVersion || isArray || isSystem)
    }
}
