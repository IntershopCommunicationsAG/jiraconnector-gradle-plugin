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

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import com.intershop.gradle.jiraconnector.util.SetIssueFieldRunner
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

@CompileStatic
class SetIssueField extends JiraConnectTask {

    @Internal
    final WorkerExecutor workerExecutor

    final RegularFileProperty issueFile = project.layout.fileProperty()

    final Property<String> linePattern = project.objects.property(String)
    final Property<String> jiraIssuePattern = project.objects.property(String)
    final Property<String> versionMessage = project.objects.property(String)
    final Property<String> fieldValue = project.objects.property(String)
    final Property<String> fieldName = project.objects.property(String)
    final Property<String> fieldPattern = project.objects.property(String)
    final Property<Boolean> mergeMilestoneVersions = project.objects.property(Boolean)

    @Inject
    SetIssueField(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
        this.description = 'Set value for a specified field of a Jira issue.'
    }

    @InputFile
    File getIssueFile() {
        return issueFile.get().getAsFile()
    }

    void setIssueFile(File issueFile) {
        this.issueFile.set(issueFile)
    }

    void setIssueFile(Provider<RegularFile> issueFile) {
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
        return mergeMilestoneVersions.getOrElse(true).booleanValue()
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

            getWorkerExecutor().submit(SetIssueFieldRunner.class, new Action<WorkerConfiguration>() {
                @Override
                void execute(WorkerConfiguration config) {
                    config.setDisplayName('Set value for a specified field of a Jira issue.')

                    config.setParams(getBaseURL(), getUsername(), getPassword(), getSocketTimeout(), getRequestTimeout(),
                                     getIssueFile(), getLinePattern(), getFieldPattern(), getJiraIssuePattern(),
                                     getFieldName(), getFieldValue(), getVersionMessage(), getMergeMilestoneVersions())


                    config.setIsolationMode(IsolationMode.CLASSLOADER)
                    config.classpath(project.getConfigurations().findByName(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION).getFiles())
                }
            })

            getWorkerExecutor().await()
        }
    }
}
