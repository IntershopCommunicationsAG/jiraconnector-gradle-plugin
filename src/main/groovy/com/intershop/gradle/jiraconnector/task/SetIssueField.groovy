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
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher

@CompileStatic
class SetIssueField extends JiraConnectTask {

    final PropertyState<File> issueFile = project.property(File)
    final PropertyState<String> linePattern = project.property(String)
    final PropertyState<String> jiraIssuePattern = project.property(String)
    final PropertyState<String> versionMessage = project.property(String)
    final PropertyState<String> fieldValue = project.property(String)
    final PropertyState<String> fieldName = project.property(String)
    final PropertyState<String> fieldPattern = project.property(String)
    final PropertyState<Boolean> mergeMilestoneVersions = project.property(Boolean)

    @InputFile
    File getIssueFile() {
        return issueFile.get()
    }

    void setIssueFile(File issueFile) {
        this.issueFile.set(issueFile)
    }

    void setIssueFile(Provider<File> issueFile) {
        this.issueFile.set(issueFile)
    }

    @Input
    String getLinePattern() {
        return linePattern.get()
    }

    void setLinePattern(String linePattern) {
        this.linePattern.set(linePattern)
    }

    void setLinePattern(Provider<String> linePattern) {
        this.linePattern.set(linePattern)
    }

    @Input
    String getJiraIssuePattern() {
        return jiraIssuePattern.get()
    }

    void setJiraIssuePattern(String jiraIssuePattern) {
        this.jiraIssuePattern.set(jiraIssuePattern)
    }

    void setJiraIssuePattern(Provider<String> jiraIssuePattern) {
        this.jiraIssuePattern.set(jiraIssuePattern)
    }

    @Input
    String getVersionMessage() {
        return versionMessage.get()
    }

    void setVersionMessage(String versionMessage) {
        this.versionMessage.set(versionMessage)
    }

    void setVersionMessage(Provider<String> versionMessage) {
        this.versionMessage.set(versionMessage)
    }

    @Input
    String getFieldValue() {
        return fieldValue.get()
    }

    void setFieldValue(String fieldValue) {
        this.fieldValue.set(fieldValue)
    }

    void setFieldValue(Provider<String> fieldValue) {
        this.fieldValue.set(fieldValue)
    }

    @Input
    String getFieldName() {
        return fieldName.get()
    }

    void setFieldName(String fieldName) {
        this.fieldName.set(fieldName)
    }

    void setFieldName(Provider<String> fieldName) {
        this.fieldName.set(fieldName)
    }

    @Input
    String getFieldPattern() {
        return fieldPattern.get()
    }

    void setFieldPattern(String fieldPattern) {
        this.fieldPattern.set(fieldPattern)
    }

    void setFieldPattern(Provider<String> fieldPattern) {
        this.fieldPattern.set(fieldPattern)
    }

    @Input
    boolean getMergeMilestoneVersions() {
        return mergeMilestoneVersions.get().booleanValue()
    }

    void setMergeMilestoneVersions(boolean mergeMilestoneVersions) {
        this.mergeMilestoneVersions.set(new Boolean(mergeMilestoneVersions))
    }

    void setMergeMilestoneVersions(Provider<Boolean> mergeMilestoneVersions) {
        this.mergeMilestoneVersions.set(mergeMilestoneVersions)
    }

    SetIssueField() {
        this.description = 'Writes text or version to specified field.'
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
            JiraConnector connector = getPreparedConnector()

            String fieldValue = getFieldValue()

            try {
                Matcher fieldMatcher = (getFieldValue() =~ /${getFieldPattern()}/)
                fieldValue = ((List)fieldMatcher[0])[1]
            } catch(Exception ex) {
                logger.warn('Fieldvalue {} is used, because field pattern does not work correctly.', fieldValue)
            }

            try {
                connector.processIssues(issueList, getFieldName(), fieldValue, getVersionMessage(), getMergeMilestoneVersions(), org.joda.time.DateTime.now())
            }catch(Exception ex) {
                throw new GradleException("It was not possible to write data to Jira server with '${ex.getMessage()}'")
            }
        } else {
            if(! getIssueFile()) throw new GradleException('Jira issue file is not configured properly.')
        }
    }
}
