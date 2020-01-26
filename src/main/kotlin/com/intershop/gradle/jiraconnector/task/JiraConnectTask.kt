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

import com.intershop.gradle.jiraconnector.util.getValue
import com.intershop.gradle.jiraconnector.util.setValue
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class JiraConnectTask : DefaultTask() {

    /**
     * Inject service of ObjectFactory (See "Service injection" in Gradle documentation.
     */
    @get:Inject
    abstract val objectFactory: ObjectFactory

    companion object {
        // server parameter
        const val SERVER_USER_NAME = "jiraUserName"

        const val SERVER_USER_PASSWORD = "jiraUserPASSWD"

        const val SERVER_BASEURL = "jiraBaseURL"

        // time out configuration
        const val SOCKET_TIMEOUT = "socketTimeout"

        const val REQUEST_TIMEOUT = "requestTimeout"
    }

    protected val baseURLProperty: Property<String> = objectFactory.property(String::class.java)
    protected val usernameProperty: Property<String> = objectFactory.property(String::class.java)
    protected val passwordProperty: Property<String> = objectFactory.property(String::class.java)

    protected val socketTimeoutProperty: Property<Int> = objectFactory.property(Int::class.java)
    protected val requestTimeoutProperty: Property<Int> = objectFactory.property(Int::class.java)

    init {
        group = "Jira Tasks"
        outputs.upToDateWhen { false }
    }

    @get:Input
    var baseURL: String by baseURLProperty

    fun provideBaseURL(baseURL: Provider<String>) = baseURLProperty.set(baseURL)

    @get:Optional
    @get:Input
    var username: String by usernameProperty

    fun provideUsername(username: Provider<String>) = usernameProperty.set(username)

    @get:Optional
    @get:Input
    var password: String by passwordProperty

    fun providePassword(username: Provider<String>) = passwordProperty.set(username)

    @get:Input
    var socketTimeout: Int by socketTimeoutProperty

    fun provideSocketTimeout(socketTimeout: Provider<Int>) = socketTimeoutProperty.set(socketTimeout)

    @get:Input
    var requestTimeout: Int by requestTimeoutProperty

    fun provideRequestTimeout(requestTimeout: Provider<Int>) = requestTimeoutProperty.set(requestTimeout)

    protected fun configure(parameters: JiraConnectorParameters) {
        if(project.findProperty(SERVER_BASEURL) != null && project.property(SERVER_BASEURL).toString().isNotEmpty()) {
            parameters.baseUrl.set( project.property(SERVER_BASEURL).toString() )
        } else {
            parameters.baseUrl.set(baseURLProperty)
        }
        if(project.findProperty(SERVER_USER_NAME) != null && project.property(SERVER_USER_NAME).toString().isNotEmpty()) {
            parameters.userName.set( project.property(SERVER_USER_NAME).toString() )
        } else {
            parameters.userName.set( usernameProperty )
        }
        if(project.findProperty(SERVER_USER_PASSWORD) != null && project.property(SERVER_USER_PASSWORD).toString().isNotEmpty()) {
            parameters.userPassword.set( project.property(SERVER_USER_PASSWORD).toString() )
        } else {
            parameters.userPassword.set( passwordProperty )
        }

        if(project.findProperty(SOCKET_TIMEOUT) != null && project.property(SOCKET_TIMEOUT).toString().isNotEmpty()) {
            parameters.socketTimeout.set( project.property(SOCKET_TIMEOUT).toString().toInt() )
        } else {
            parameters.socketTimeout.set( socketTimeoutProperty )
        }
        if(project.findProperty(REQUEST_TIMEOUT) != null && project.property(REQUEST_TIMEOUT).toString().isNotEmpty()) {
            parameters.requestTimeout.set( project.property(REQUEST_TIMEOUT).toString().toInt() )
        } else {
            parameters.requestTimeout.set( requestTimeoutProperty )
        }
    }
}