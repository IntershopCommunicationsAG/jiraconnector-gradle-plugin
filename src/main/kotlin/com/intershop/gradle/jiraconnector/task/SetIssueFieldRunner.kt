/*
 * Copyright 2019 Intershop Communications AG.
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
package com.intershop.gradle.jiraconnector.task

import com.intershop.gradle.jiraconnector.task.jira.JiraConnector
import com.intershop.gradle.jiraconnector.task.jira.JiraIssueParser
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class SetIssueFieldRunner: WorkAction<SetIssueFieldParameters> {

    companion object {
        /**
         * Logger instance for logging.
         */
        val log: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    override fun execute() {
        var fieldValueInt: String? = null

        val issueList = JiraIssueParser.parse(
                parameters.issueFile.get().asFile,
                parameters.linePattern.get(),
                parameters.jiraIssuePattern.get())

        val connector = getPreparedConnector()

        if(parameters.fieldPattern.get().isNotEmpty()) {
            val regex = Regex(parameters.fieldPattern.get())
            fieldValueInt = regex.find(parameters.fieldValue.get())?.value
        }

        if(fieldValueInt == null) {
            fieldValueInt = parameters.fieldValue.get()
            log.warn("Fieldvalue {} is used, because field pattern does not work correctly.", fieldValueInt)
        }

        if(connector != null && fieldValueInt != null) {
            try {
                connector.processIssues(issueList, parameters.fieldName.get(), fieldValueInt,
                    parameters.versionMessage.get(), parameters.mergeMilestoneVersions.get(), DateTime ())
            } catch (ex: Exception) {
                throw GradleException ("It was not possible to write data to Jira server with '${ex.message}'")
            }
        } else {
            throw GradleException ("It was not possible to initialize the process. Please check the configuration.")
        }
    }

    private fun getPreparedConnector(): JiraConnector? {
        if(parameters.baseUrl.isPresent &&
                parameters.userName.isPresent &&
                parameters.userPassword.isPresent ) {
            val connector = JiraConnector(
                    parameters.baseUrl.get(),
                    parameters.userName.get(),
                    parameters.userPassword.get())

            if(parameters.socketTimeout.isPresent) {
                connector.socketTimeout = parameters.socketTimeout.get()
            }
            if(parameters.requestTimeout.isPresent) {
                connector.requestTimeout = parameters.requestTimeout.get()
            }

            return connector
        }
        return null
    }
}