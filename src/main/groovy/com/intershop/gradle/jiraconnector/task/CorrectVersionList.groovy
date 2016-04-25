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

package com.intershop.gradle.jiraconnector.task

import com.intershop.gradle.jiraconnector.util.JiraConnector
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class CorrectVersionList extends DefaultTask {

    @Input
    String baseURL

    @Input
    String username

    @Input
    String password

    @Input
    String projectKey

    @Optional
    @Input
    Map<String, String> replacements

    CorrectVersionList() {
        this.description = 'Correct Jira version list.'
        this.group = 'Jira Tasks'
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void correctVersionList() {
        if(getBaseURL() && getUsername() && getPassword()) {
            JiraConnector connector = new JiraConnector(getBaseURL(), getUsername(), getPassword())
            connector.sortVersions(getProjectKey())
            if(getReplacements()) {
                connector.fixVersionNames(getProjectKey(), getReplacements())
            }
        } else {
            if(! getBaseURL()) throw new GradleException('Jira base url is missing')
            if(!(getUsername() && getPassword())) {
                throw new GradleException("Jira credentials for ${getBaseURL()} are not configured properly.")
            }
        }
    }
}
