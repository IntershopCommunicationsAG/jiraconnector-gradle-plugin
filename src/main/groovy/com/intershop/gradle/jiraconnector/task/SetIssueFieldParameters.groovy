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

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface SetIssueFieldParameters extends WorkParameters {

    Property<String> getBaseURL()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<File> getIssueFile()

    Property<String> getLinePattern()

    Property<String> getFieldPattern()

    Property<String> getJiraIssuePattern()

    Property<String> getFieldValue()

    Property<String> getFieldName()

    Property<String> getVersionMessage()

    Property<Boolean> getMergeMilestoneVersions()

    Property<Integer> getSocketTimeout()

    Property<Integer> getRequestTimeout()
}