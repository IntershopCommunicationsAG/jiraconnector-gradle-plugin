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
package com.intershop.gradle.jiraconnector.extension

import groovy.util.logging.Slf4j
import org.gradle.api.Project

@Slf4j
class JiraConnectorExtension {

    private Project project

    // run on CI server
    public final static String RUNONCI_ENV = 'RUNONCI'
    public final static String RUNONCI_PRJ = 'runOnCI'

    // Task names
    public static final String JIRACONNECTOR_SETISSUEFIELD = 'setIssueField'
    public static final String JIRACONNECTOR_CORRECTVERSIONLIST = 'correctVersionList'

    // extension  name
    public static final String JIRACONNECTOR_EXTENSION_NAME = 'jiraConnector'
    // Patternf for Atlassian JIIRA issues
    public static final String JIRAISSUE_PATTERN = '([A-Z][A-Z0-9]+)-([0-9]+)'

    // default string for messages
    public static final String JIRAVERSIONMESSAGE = 'created by jiraconnector plugin'

    /**
     * <p>Configuration for the execution on the CI server</p>
     *
     * <p>Can be configured/overwritten with environment variable RUNONCI;
     * java environment RUNONCI or project variable runOnCI</p>
     */
    boolean runOnCI

    /**
     * Server configuration
     */
    Server server

    /**
     * Line Pattern for Jira Issues
     */
    String linePattern

    /**
     * Jira field name
     */
    String fieldName

    /**
     * Text pattern
     */
    String fieldValue

    /**
     * String pattern for version message
     */
    String fieldPattern

    /**
     * String value for Jira version editing
     */
    String versionMessage

    /**
     * Merge previous mile stone versions
     */
    boolean mergeMilestoneVersions = true

    /**
     * File with Jira Issue references
     */
    File issueFile

    /**
     * Map with replacements with component names
     */
    Map<String, String> replacements

    JiraConnectorExtension(Project project) {
        this.project = project

        // init default value for runOnCI
        if(! runOnCI) {
            runOnCI = Boolean.parseBoolean(getVariable(project, RUNONCI_ENV, RUNONCI_PRJ, 'false'))
        }

        if(! versionMessage) {
            versionMessage = JIRAVERSIONMESSAGE
        }

        // initialize server configuration
        server = new Server(project)
    }

    Server server(Closure closure) {
        project.configure(server, closure)
    }

    /**
     * Calculates the setting for special configuration from the system
     * or java environment or project properties.
     *
     * @param envVar        name of environment variable
     * @param projectVar    name of project variable
     * @param defaultValue  default value
     * @return              the string configuration
     */
    static String getVariable(Project project, String envVar, String projectVar, String defaultValue = '') {
        if(System.properties[envVar]) {
            log.debug('Specified from system property {}.', envVar)
            return System.properties[envVar].toString().trim()
        } else if(System.getenv(envVar)) {
            log.debug('Specified from system environment property {}.', envVar)
            return System.getenv(envVar).toString().trim()
        } else if(project.hasProperty(projectVar) && project."${projectVar}") {
            log.debug('Specified from project property {}.', projectVar)
            return project."${projectVar}".toString().trim()
        }
        return defaultValue
    }
}
