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

import com.intershop.gradle.jiraconnector.task.CorrectVersionListParameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.workers.WorkAction

@CompileStatic
@Slf4j
abstract class CorrectVersionListRunner implements WorkAction<CorrectVersionListParameters> {

    @Override
    void execute() {
        JiraConnector connector = getPreparedConnector()
        connector.sortVersions(getParameters().getProjectKey().get())
        if(getParameters().getReplacements().getOrNull() != null) {
            connector.fixVersionNames(getParameters().getProjectKey().get(), getParameters().getReplacements().get())
        }
    }

    private JiraConnector getPreparedConnector() {
        if(getParameters().getBaseURL().get() &&
                getParameters().getUsername().get() &&
                getParameters().getPassword().get()) {
            JiraConnector connector = new JiraConnector(
                    getParameters().getBaseURL().get(),
                    getParameters().getUsername().get(),
                    getParameters().getPassword().get())

            if(getParameters().getSocketTimeout()) {
                connector.setSocketTimeout(getParameters().getSocketTimeout().get())
            }
            if(getParameters().getRequestTimeout()) {
                connector.setRequestTimeout(getParameters().getRequestTimeout().get())
            }

            return connector
        }
        return null
    }
}
