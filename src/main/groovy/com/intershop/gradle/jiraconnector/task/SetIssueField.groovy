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
import com.intershop.gradle.jiraconnector.util.JiraIssueParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher

class SetIssueField extends DefaultTask {

    @InputFile
    File issueFile

    @Input
    String baseURL

    @Input
    String username

    @Input
    String password

    @Input
    String linePattern

    @Input
    String jiraIssuePattern

    @Input
    String versionMessage

    @Input
    String fieldValue

    @Input
    String fieldName

    @Input
    String fieldPattern

    @Input
    boolean mergeMilestoneVersions

    SetIssueField() {
        this.description = 'Writes text or version to specified field.'
        this.group = 'Jira Conntector Tasks'
        this.outputs.upToDateWhen { false }
    }

    @TaskAction
    void editIssue() {
        if(getIssueFile() && getBaseURL() && getUsername() && getPassword()) {
            if(! getFieldValue()) {
                throw new GradleException('Please specify a Jira field value.')
            }
            if(! getFieldName()) {
                throw new GradleException('Please specify a name for the Jira field.')
            }

            List<String> issueList = JiraIssueParser.parse(getIssueFile(), getLinePattern(), getJiraIssuePattern())
            JiraConnector connector = new JiraConnector(getBaseURL(), getUsername(), getPassword())
            try {
                Matcher fieldMatcher = (getFieldValue() =~ /${getFieldPattern()}/)
                connector.processIssues(issueList, getFieldName(), fieldMatcher[0][1], getVersionMessage(), getMergeMilestoneVersions(), org.joda.time.DateTime.now())
            }catch(Exception ex) {
                throw new GradleException("It was not possible to write data to Jira server with '${ex.getMessage()}'")
            }
        } else {
            if(! getIssueFile()) throw new GradleException('Jira issue file is not configured properly.')
            if(! getBaseURL()) throw new GradleException('Jira base url is missing')
            if(!(getUsername() && getPassword())) {
                throw new GradleException("Jira credentials for ${getBaseURL()} are not configured properly.")
            }
        }
    }
}
