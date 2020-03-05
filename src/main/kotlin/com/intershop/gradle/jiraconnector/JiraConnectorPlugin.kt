/*
 * Copyright 2020 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intershop.gradle.jiraconnector

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension.Companion.JIRAISSUE_PATTERN
import com.intershop.gradle.jiraconnector.task.CorrectVersionList
import com.intershop.gradle.jiraconnector.task.JiraConnectTask
import com.intershop.gradle.jiraconnector.task.JiraConnectTask.Companion.SERVER_BASEURL
import com.intershop.gradle.jiraconnector.task.JiraConnectTask.Companion.SERVER_USER_NAME
import com.intershop.gradle.jiraconnector.task.JiraConnectTask.Companion.SERVER_USER_PASSWORD
import com.intershop.gradle.jiraconnector.task.SetIssueField
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySet

/**
 * Main implementation of the plugin.
 */
class JiraConnectorPlugin: Plugin<Project> {

    companion object {
        /**
         * Task name for setIssueField task.
         */
        const val JIRACONNECTOR_SETISSUEFIELD = "setIssueField"

        /**
         * Task name for correctVersionList task.
         */
        const val JIRACONNECTOR_CORRECTVERSIONLIST = "correctVersionList"

        private fun checkProperty(project: Project, propName: String): Boolean {
            return project.hasProperty(propName) && project.property(propName).toString().isNotEmpty()
        }
    }

    override fun apply(project: Project) {
        if(project != project.rootProject) {
            project.logger.warn("Don't apply this Jira Connector plugin to a sub project." +
                    "All configurations will be applied to the root project.")
        }
        with(project.rootProject) {
            logger.info("Add extension {} for {}", JiraConnectorExtension.JIRACONNECTOR_EXTENSION_NAME, name)
            val extension = extensions.findByType(JiraConnectorExtension::class.java) ?: 
                    extensions.create(
                            JiraConnectorExtension.JIRACONNECTOR_EXTENSION_NAME,
                            JiraConnectorExtension::class.java)

            addJiraRestClientConfiguration(this)

            createWritToJiraTask(this, extension)
            createCorrectVersionList(this, extension)
        }
    }

    private fun createWritToJiraTask(project: Project, extension: JiraConnectorExtension) {
        project.tasks.maybeCreate(JIRACONNECTOR_SETISSUEFIELD, SetIssueField::class.java).apply {
            configureTaskConnection(this, extension)

            provideIssueFile(extension.issueFileProvider)
            provideLinePattern(extension.linePatternProvider)
            jiraIssuePattern = JIRAISSUE_PATTERN
            provideVersionMessage(extension.versionMessageProvider)
            provideFieldName(extension.fieldNameProvider)
            provideFieldValue(extension.fieldValueProvider)
            provideFieldPattern(extension.fieldPatternProvider)
            provideMergeMilestoneVersions(extension.mergeMilestoneVersionsProvider)

            onlyIf {
                (extension.server.baseURL.isNotEmpty() || checkProperty(project, SERVER_BASEURL)) &&
                        (extension.server.username.isNotEmpty() || checkProperty(project, SERVER_USER_NAME)) &&
                        (extension.server.password.isNotEmpty() || checkProperty(project, SERVER_USER_PASSWORD))
            }
        }
    }

    private fun createCorrectVersionList(project: Project, extension: JiraConnectorExtension) {
        project.tasks.maybeCreate(JIRACONNECTOR_CORRECTVERSIONLIST, CorrectVersionList::class.java).apply {
            configureTaskConnection(this, extension)

            provideReplacements(extension.replacementsProvider)

            onlyIf {
                (extension.server.baseURL.isNotEmpty() || checkProperty(project, SERVER_BASEURL)) &&
                        (extension.server.username.isNotEmpty() || checkProperty(project, SERVER_USER_NAME)) &&
                        (extension.server.password.isNotEmpty() || checkProperty(project, SERVER_USER_PASSWORD))
            }
        }
    }

    private fun configureTaskConnection(task: JiraConnectTask, extension: JiraConnectorExtension) {
        task.provideBaseURL(extension.server.baseURLProvider)
        task.provideUsername(extension.server.usernameProvider)
        task.providePassword(extension.server.passwordProvider)

        task.provideSocketTimeout(extension.server.socketTimeoutProvider)
        task.provideRequestTimeout(extension.server.requestTimeoutProvider)
    }

    private fun addJiraRestClientConfiguration(project: Project) {
        val configuration = project.configurations.maybeCreate(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION)
        configuration
                .setVisible(false)
                .setTransitive(true)
                .setDescription("Atlassian Jira Rest client libraries")
                .defaultDependencies { dependencies: DependencySet ->
                    // this will be executed if configuration is empty
                    val dependencyHandler = project.dependencies
                    dependencies.add(dependencyHandler.create("com.atlassian.jira:jira-rest-java-client-core:5.2.1"))
                    dependencies.add(dependencyHandler.create("com.atlassian.jira:jira-rest-java-client-api:5.2.1"))
                    dependencies.add(dependencyHandler.create("io.atlassian.fugue:fugue:4.7.2"))
                }
    }
}
