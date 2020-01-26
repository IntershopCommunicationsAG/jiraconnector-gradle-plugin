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

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File

interface SetIssueFieldParameters  : JiraConnectorParameters {

    val issueFile: RegularFileProperty

    val linePattern: Property<String>

    val fieldPattern: Property<String>

    val jiraIssuePattern: Property<String>

    val fieldValue: Property<String>

    val fieldName: Property<String>

    val versionMessage: Property<String>

    val mergeMilestoneVersions: Property<Boolean>
}