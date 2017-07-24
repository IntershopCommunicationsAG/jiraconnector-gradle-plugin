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
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the implementation of the plugin.
 */
@CompileStatic
class JiraConnectorPlugin  implements Plugin<Project> {

    private JiraConnectorExtension extension

    public void apply(Project project) {
        if(project.name != project.rootProject.name) {
            project.logger.warn("Don't apply this Jira Connector plugin to a sub project. All configurations will be applied to the root project.")
        }

        Project configProject = project.rootProject

        configProject.logger.info('Create extension {} for {}', JiraConnectorExtension.JIRACONNECTOR_EXTENSION_NAME, configProject.name)
        extension = configProject.extensions.findByType(JiraConnectorExtension) ?: configProject.extensions.create(JiraConnectorExtension.JIRACONNECTOR_EXTENSION_NAME, JiraConnectorExtension, configProject)

        if (extension.runOnCI) {
            createWritToJiraTask(project)
            createCorrectVersionList(project)
        }
    }

    private void createWritToJiraTask(Project project) {
        def task = project.tasks.maybeCreate(JiraConnectorExtension.JIRACONNECTOR_SETISSUEFIELD, SetIssueField.class)

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
        def task = project.tasks.maybeCreate(JiraConnectorExtension.JIRACONNECTOR_CORRECTVERSIONLIST, CorrectVersionList.class)

        task.setUsername( extension.server.getUsernameProvider() )
        task.setPassword( extension.server.getPasswordProvider() )
        task.setBaseURL( extension.server.getBaseURLProvider() )

        task.setSocketTimeout( extension.server.getSocketTimeoutProvider() )
        task.setRequestTimeout( extension.server.getRequestTimeoutProvider() )

        task.setReplacements( extension.getReplacementsProvider() )
    }
}
