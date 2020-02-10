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

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * List of parameters for the workerexecutor of the SetIssueField task.
 */
interface SetIssueFieldParameters  : JiraConnectorParameters {

    /**
     * This is the property with text file with Jira issues.
     *
     * @property issueFile
     */
    val issueFile: RegularFileProperty

    /**
     * This is the line pattern property for
     * the text file with Jira issues.
     *
     * @property linePattern
     */
    val linePattern: Property<String>

    /**
     * This is the field pattern property for the
     * selection of the field.
     *
     * @property fieldPattern
     */
    val fieldPattern: Property<String>

    /**
     * This is the pattern property of a Jira Issue in a text file.
     *
     * @property jiraIssuePattern
     */
    val jiraIssuePattern: Property<String>

    /**
     * This is the value for the selected fields of the selected Jira issues.
     *
     * @property fieldValue
     */
    val fieldValue: Property<String>

    /**
     * This is the field name, that will be changed of the selected Jira issues.
     *
     * @property fieldName
     */
    val fieldName: Property<String>

    /**
     * This is the message, that is used for changes in Jira.
     *
     * @property versionMessage
     */
    val versionMessage: Property<String>

    /**
     * If this property is true, milestone versions will be merged.
     *
     * @property mergeMilestoneVersions
     */
    val mergeMilestoneVersions: Property<Boolean>
}
