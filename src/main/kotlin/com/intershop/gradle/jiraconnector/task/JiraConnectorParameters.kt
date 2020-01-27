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

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

/**
 * List of parameters for the workerexecutor of all Jira tasks.
 */
interface JiraConnectorParameters : WorkParameters {

    /**
     * This is the baseUrl property of the Jira connection.
     *
     * @property baseUrl
     */
    val baseUrl: Property<String>

    /**
     * This is the userName property of the Jira connection.
     *
     * @property userName
     */
    val userName: Property<String>

    /**
     * This is the userPassword property of the Jira connection.
     *
     * @property userPassword
     */
    val userPassword: Property<String>

    /**
     * This is the socketTimeout property of the Jira connection.
     *
     * @property socketTimeout
     */
    val socketTimeout: Property<Int>

    /**
     * This is the requestTimeout property of the Jira connection.
     *
     * @property requestTimeout
     */
    val requestTimeout: Property<Int>

}
