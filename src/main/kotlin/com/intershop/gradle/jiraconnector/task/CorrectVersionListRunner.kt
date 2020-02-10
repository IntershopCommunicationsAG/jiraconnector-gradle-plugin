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
package com.intershop.gradle.jiraconnector.task

import com.intershop.gradle.jiraconnector.task.jira.JiraConnector
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction

/**
 * Work action implementation for the CorrectVersionList task.
 */
abstract class CorrectVersionListRunner: WorkAction<CorrectVersionListParameters> {

    override fun execute() {
        val connector = getPreparedConnector()

        if (connector != null) {
            connector.sortVersions(parameters.projectKey.get())
            if(parameters.replacements.orNull != null) {
                connector.fixVersionNames(parameters.projectKey.get(), parameters.replacements.get())
            }
        } else {
            throw GradleException("It was not possible to initialize the process. Please check the configuration.")
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
