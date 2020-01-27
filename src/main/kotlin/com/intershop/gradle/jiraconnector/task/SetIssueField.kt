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

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import com.intershop.gradle.jiraconnector.util.getValue
import com.intershop.gradle.jiraconnector.util.setValue
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

/**
 * This tasks changes a special field with a value of a list
 * of Jira issues.
 *
 * @constructor creates a class with an injected workerexecutor.
 */
abstract class SetIssueField @Inject constructor(private val workerExecutor: WorkerExecutor): JiraConnectTask() {

    private val issueFileProperty: RegularFileProperty = objectFactory.fileProperty()

    private val linePatternProperty: Property<String> = objectFactory.property(String::class.java)
    private val jiraIssuePatternProperty: Property<String> = objectFactory.property(String::class.java)
    private val versionMessageProperty: Property<String> = objectFactory.property(String::class.java)
    private val fieldValueProperty: Property<String> = objectFactory.property(String::class.java)
    private val fieldNameProperty: Property<String> = objectFactory.property(String::class.java)
    private val fieldPatternProperty: Property<String> = objectFactory.property(String::class.java)
    private val mergeMilestoneVersionsProperty: Property<Boolean> = objectFactory.property(Boolean::class.java)

    init {
        description = "This task add an value to a special field of an Jira issue."
        mergeMilestoneVersionsProperty.convention(true)
    }

    /**
     * Set the file with Jira issues for the Jira task.
     *
     * @property issueFile
     */
    @get:InputFile
    var issueFile: File
        get() = issueFileProperty.get().asFile
        set(value) = issueFileProperty.set(value)

    /**
     * Sets the provider for the issueFile property.
     *
     * @param issueFile provider
     */
    fun provideIssueFile(issueFile: Provider<RegularFile>) = issueFileProperty.set(issueFile)

    /**
     * Set the line pattern for lines with Jira issues.
     *
     * @property linePattern
     */
    @get:Input
    var linePattern: String by linePatternProperty

    /**
     * Sets the provider for the linePattern property.
     *
     * @param linePattern provider
     */
    fun provideLinePattern(linePattern: Provider<String>) = linePatternProperty.set(linePattern)

    /**
     * Set the pattern for Jira issues.
     *
     * @property jiraIssuePattern
     */
    @get:Input
    var jiraIssuePattern: String by jiraIssuePatternProperty

    /**
     * Sets the provider for the jiraIssuePattern property.
     *
     * @param jiraIssuePattern provider
     */
    fun provideJiraIssuePattern(jiraIssuePattern: Provider<String>) = jiraIssuePatternProperty.set(jiraIssuePattern)

    /**
     * Set the version message for changed Jira issues.
     *
     * @property jiraIssuePattern
     */
    @get:Input
    var versionMessage: String by versionMessageProperty

    /**
     * Sets the provider for the versionMessage property.
     *
     * @param versionMessage provider
     */
    fun provideVersionMessage(versionMessage: Provider<String>) = versionMessageProperty.set(versionMessage)

    /**
     * Set the field value for Jira issues.
     *
     * @property fieldValue
     */
    @get:Input
    var fieldValue: String by fieldValueProperty

    /**
     * Sets the provider for the fieldValue property.
     *
     * @param fieldValue provider
     */
    fun provideFieldValue(fieldValue: Provider<String>) = fieldValueProperty.set(fieldValue)

    /**
     * Set the field name for Jira issues.
     *
     * @property fieldName
     */
    @get:Input
    var fieldName: String by fieldNameProperty

    /**
     * Sets the provider for the fieldName property.
     *
     * @param fieldName provider
     */
    fun provideFieldName(fieldName: Provider<String>) = fieldNameProperty.set(fieldName)

    /**
     * Set the field pattern for Jira issues.
     *
     * @property fieldPattern
     */
    @get:Input
    var fieldPattern: String by fieldPatternProperty

    /**
     * Sets the provider for the fieldPattern property.
     *
     * @param fieldPattern provider
     */
    fun provideFieldPattern(fieldPattern: Provider<String>) = fieldPatternProperty.set(fieldPattern)

    /**
     * If this property is true milestone versions will be merged for Jira issues.
     *
     * @property mergeMilestoneVersions
     */
    @get:Input
    var mergeMilestoneVersions: Boolean
        get() = mergeMilestoneVersionsProperty.get()
        set(value) = mergeMilestoneVersionsProperty.set(value)

    /**
     * Sets the provider for the mergeMilestoneVersions property.
     *
     * @param mergeMilestoneVersions provider
     */
    fun provideMergeMilestoneVersions(mergeMilestoneVersions: Provider<Boolean>) =
            mergeMilestoneVersionsProperty.set(mergeMilestoneVersions)

    /**
     * Implementation of the task action.
     */
    @TaskAction
    fun editIssue() {
        // start runner
        val workQueue = workerExecutor.classLoaderIsolation {
            it.classpath.from(project.configurations.
                    findByName(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION)?.files)
        }

        workQueue.submit(SetIssueFieldRunner::class.java) {
            configure(it)

            it.issueFile.set(issueFileProperty)
            it.linePattern.set(linePatternProperty)
            it.fieldPattern.set(fieldPatternProperty)
            it.jiraIssuePattern.set(jiraIssuePatternProperty)
            it.fieldValue.set(fieldValueProperty)
            it.fieldName.set(fieldNameProperty)
            it.versionMessage.set(versionMessageProperty)
            it.mergeMilestoneVersions.set(mergeMilestoneVersionsProperty)
        }

        workerExecutor.await()
    }
}
