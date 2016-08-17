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
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the implementation of the plugin.
 */
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

        task.conventionMapping.username = { extension.server.username }
        task.conventionMapping.password = { extension.server.password }
        task.conventionMapping.baseURL = { extension.server.baseURL }

        task.conventionMapping.socketTimeout = { extension.server.socketTimeout }
        task.conventionMapping.requestTimeout = { extension.server.requestTimeout }

        task.conventionMapping.issueFile = { extension.issueFile }

        task.conventionMapping.linePattern = { extension.getLinePattern() ?: '' }
        task.jiraIssuePattern = JiraConnectorExtension.JIRAISSUE_PATTERN
        task.conventionMapping.versionMessage = { extension.getVersionMessage() ?: extension.JIRAVERSIONMESSAGE }

        task.conventionMapping.fieldName = { extension.getFieldName() }
        task.conventionMapping.fieldValue = { extension.getFieldValue() }
        task.conventionMapping.fieldPattern = { extension.getFieldPattern() ?: '(.*)' }

        task.conventionMapping.mergeMilestoneVersions = { extension.getMergeMilestoneVersions() }

        task.onlyIf {
            extension.server.getBaseURL() &&
                    extension.server.getUsername() &&
                    extension.server.getPassword() &&
                    ! project.getVersion().toString().toLowerCase().endsWith('snapshot')
        }
    }

    private void createCorrectVersionList(Project project) {
        def task = project.tasks.maybeCreate(JiraConnectorExtension.JIRACONNECTOR_CORRECTVERSIONLIST, CorrectVersionList.class)

        task.conventionMapping.username = { extension.server.username }
        task.conventionMapping.password = { extension.server.password }
        task.conventionMapping.baseURL = { extension.server.baseURL }

        task.conventionMapping.socketTimeout = { extension.getSocketTimeout() }
        task.conventionMapping.requestTimeout = { extension.getRequestTimeout() }

        task.conventionMapping.replacements = { extension.replacements ?: [:] }
    }
}
