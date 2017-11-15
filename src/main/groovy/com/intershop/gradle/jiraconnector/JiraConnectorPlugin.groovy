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
package com.intershop.gradle.jiraconnector

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import com.intershop.gradle.jiraconnector.task.CorrectVersionList
import com.intershop.gradle.jiraconnector.task.SetIssueField
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.workers.WorkerConfiguration

/**
 * This is the implementation of the plugin.
 */
@CompileStatic
class JiraConnectorPlugin  implements Plugin<Project> {

    // run on CI server
    public final static String RUNONCI_ENV = 'RUNONCI'
    public final static String RUNONCI_PRJ = 'runOnCI'

    // server parameter
    public final static String SERVER_USER_NAME_ENV = 'JIRAUSERNAME'
    public final static String SERVER_USER_NAME_PRJ = 'jiraUserName'

    public final static String SERVER_USER_PASSWORD_ENV = 'JIRAUSERPASSWD'
    public final static String SERVER_USER_PASSWORD_PRJ = 'jiraUserPASSWD'

    public final static String SERVER_BASEURL_ENV = 'JIRABASEURL'
    public final static String SERVER_BASEURL_PRJ = 'jiraBaseURL'

    // time out configuration
    public final static String SOCKET_TIMEOUT_ENV = 'SOCKET_TIMEOUT'
    public final static String SOCKET_TIMEOUT_PRJ = 'socketTimeout'

    public final static String REQUEST_TIMEOUT_ENV = 'REQUEST_TIMEOUT'
    public final static String REQUEST_TIMEOUT_PRJ = 'requestTimeout'

    // Task names
    public static final String JIRACONNECTOR_SETISSUEFIELD = 'setIssueField'
    public static final String JIRACONNECTOR_CORRECTVERSIONLIST = 'correctVersionList'

    private JiraConnectorExtension extension

    void apply(Project project) {
        if(project.name != project.rootProject.name) {
            project.logger.warn("Don't apply this Jira Connector plugin to a sub project. All configurations will be applied to the root project.")
        }

        Project configProject = project.rootProject

        configProject.logger.info('Create extension {} for {}', JiraConnectorExtension.JIRACONNECTOR_EXTENSION_NAME, configProject.name)
        extension = configProject.extensions.findByType(JiraConnectorExtension) ?: configProject.extensions.create(JiraConnectorExtension.JIRACONNECTOR_EXTENSION_NAME, JiraConnectorExtension, configProject)

        // add configuration with dependencies, this can be overwritten
        addJiraRestClientConfiguration(configProject)

        // initialize extension with values
        if(! extension.getRunOnCIProvider().getOrNull()) {
            extension.setRunOnCI(new Boolean(getVariable(project, RUNONCI_ENV, RUNONCI_PRJ, 'false')))
        }
        if(! extension.getVersionMessageProvider().getOrNull()) {
            extension.setVersionMessage(extension.JIRAVERSIONMESSAGE)
        }

        if(! extension.server.getBaseURLProvider().getOrNull()) {
            extension.server.setBaseURL(getVariable(project, SERVER_BASEURL_ENV, SERVER_BASEURL_PRJ))
        }
        if(! extension.server.getUsernameProvider().getOrNull()) {
            extension.server.setUsername(getVariable(project, SERVER_USER_NAME_ENV, SERVER_USER_NAME_PRJ))
        }
        if(! extension.server.getPasswordProvider().getOrNull()) {
            extension.server.setPassword(getVariable(project, SERVER_USER_PASSWORD_ENV, SERVER_USER_PASSWORD_PRJ))
        }

        if(! extension.server.getSocketTimeoutProvider().getOrNull()) {
            try {
                extension.server.setSocketTimeout(new Integer(getVariable(project, SOCKET_TIMEOUT_ENV, SOCKET_TIMEOUT_PRJ, '3')))
            } catch (NumberFormatException nfe) {
                project.logger.info('Use standard value (3 minutes) for socket timeout')
                extension.server.setSocketTimeout(new Integer(3))
            }
        }
        if(! extension.server.getSocketTimeoutProvider().getOrNull()) {
            try {
                extension.server.setRequestTimeout(new Integer(getVariable(project, REQUEST_TIMEOUT_ENV, REQUEST_TIMEOUT_PRJ, '3')))
            } catch (NumberFormatException nfe) {
                project.logger.info('Use standard value (3 minutes) for request timeout')
                extension.server.setRequestTimeout(new Integer(3))
            }
        }

        if (extension.getRunOnCI()) {
            createWritToJiraTask(project)
            createCorrectVersionList(project)
        }
    }

    private void createWritToJiraTask(Project project) {
        def task = project.tasks.maybeCreate(JIRACONNECTOR_SETISSUEFIELD, SetIssueField.class)

        task.setUsername( extension.server.getUsernameProvider() )
        task.setPassword( extension.server.getPasswordProvider() )
        task.setBaseURL( extension.server.getBaseURLProvider() )

        task.setSocketTimeout( extension.server.getSocketTimeoutProvider() )
        task.setRequestTimeout( extension.server.getRequestTimeoutProvider() )

        task.setIssueFile( extension.getIssueFileProvider() )

        task.setLinePattern( extension.getLinePatternProvider() )
        task.setJiraIssuePattern( JiraConnectorExtension.JIRAISSUE_PATTERN )
        task.setVersionMessage( extension.getVersionMessageProvider() )

        task.setFieldName( extension.getFieldNameProvider() )
        task.setFieldValue(  extension.getFieldValueProvider() )
        task.setFieldPattern( extension.getFieldPatternProvider() )

        task.setMergeMilestoneVersions( extension.getMergeMilestoneVersionsProvider() )

        task.onlyIf {
            extension.server.getBaseURL() &&
                    extension.server.getUsername() &&
                    extension.server.getPassword() &&
                    ! project.getVersion().toString().toLowerCase().endsWith('snapshot')
        }
    }

    private void createCorrectVersionList(Project project) {
        def task = project.tasks.maybeCreate(JIRACONNECTOR_CORRECTVERSIONLIST, CorrectVersionList.class)

        task.setUsername( extension.server.getUsernameProvider() )
        task.setPassword( extension.server.getPasswordProvider() )
        task.setBaseURL( extension.server.getBaseURLProvider() )

        task.setSocketTimeout( extension.server.getSocketTimeoutProvider() )
        task.setRequestTimeout( extension.server.getRequestTimeoutProvider() )

        task.setReplacements( extension.getReplacementsProvider() )
    }

    private static void addJiraRestClientConfiguration(final Project project) {
        final Configuration configuration =
                project.getConfigurations().findByName(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION) ?:
                        project.getConfigurations().create(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION)

        if(configuration.getAllDependencies().isEmpty()) {
            configuration
                    .setTransitive(true)
                    .setDescription("Atlassian Jira Rest client libraries")
                    .defaultDependencies(new Action<DependencySet>() {
                @Override
                void execute(DependencySet dependencies ) {
                    DependencyHandler dependencyHandler = project.getDependencies()

                    dependencies.add(dependencyHandler.create('com.atlassian.jira:jira-rest-java-client-core:4.0.0'))
                    dependencies.add(dependencyHandler.create('com.atlassian.jira:jira-rest-java-client-api:4.0.0'))
                    dependencies.add(dependencyHandler.create('com.atlassian.fugue:fugue:2.6.1'))

                }
            })
        }
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
            project.logger.debug('Specified from system property {}.', envVar)
            return System.properties[envVar].toString().trim()
        } else if(System.getenv(envVar)) {
            project.logger.debug('Specified from system environment property {}.', envVar)
            return System.getenv(envVar).toString().trim()
        } else if(project.hasProperty(projectVar) && project.property(projectVar)) {
            project.logger.debug('Specified from project property {}.', projectVar)
            return project.property(projectVar).toString().trim()
        }
        return defaultValue
    }
}
