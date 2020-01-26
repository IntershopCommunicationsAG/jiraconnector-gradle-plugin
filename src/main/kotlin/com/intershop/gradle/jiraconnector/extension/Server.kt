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
package com.intershop.gradle.jiraconnector.extension

import com.intershop.gradle.jiraconnector.util.getValue
import com.intershop.gradle.jiraconnector.util.setValue
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import javax.inject.Inject

abstract class Server {

    companion object {
        // server parameter
        const val SERVER_USER_NAME = "JIRAUSERNAME"

        const val SERVER_USER_PASSWORD = "JIRAUSERPASSWD"

        const val SERVER_BASEURL = "JIRABASEURL"

        // time out configuration
        const val SOCKET_TIMEOUT = "SOCKET_TIMEOUT"

        const val REQUEST_TIMEOUT = "REQUEST_TIMEOUT"
    }
    /**
     * Inject service of ObjectFactory (See "Service injection" in Gradle documentation.
     */
    @get:Inject
    abstract val objectFactory: ObjectFactory

    private val baseURLProperty: Property<String> = objectFactory.property(String::class.java)
    private val usernameProperty: Property<String> = objectFactory.property(String::class.java)
    private val passwordProperty: Property<String> = objectFactory.property(String::class.java)

    private val socketTimeoutProperty: Property<Int> = objectFactory.property(Int::class.java)
    private val requestTimeoutProperty: Property<Int> = objectFactory.property(Int::class.java)

    init {
        baseURLProperty.convention((System.getProperty(SERVER_BASEURL)
                ?: System.getenv(SERVER_BASEURL) ?: "").toString().trim())

        usernameProperty.convention((System.getProperty(SERVER_USER_NAME)
                ?: System.getenv(SERVER_USER_NAME) ?: "").toString().trim())
        passwordProperty.convention((System.getProperty(SERVER_USER_PASSWORD)
                ?: System.getenv(SERVER_USER_PASSWORD) ?: "").toString().trim())

        val socketTimeoutStr = (System.getProperty(SOCKET_TIMEOUT)
                ?: System.getenv(SOCKET_TIMEOUT) ?: "3").toString().trim()
        socketTimeoutProperty.convention(socketTimeoutStr.toInt())

        val requestTimeoutStr = (System.getProperty(REQUEST_TIMEOUT)
                ?: System.getenv(REQUEST_TIMEOUT) ?: "3").toString().trim()
        requestTimeoutProperty.convention(requestTimeoutStr.toInt())
    }

    val baseURLProvider: Provider<String>
        get() = baseURLProperty

    var baseURL by baseURLProperty

    val usernameProvider: Provider<String>
        get() = usernameProperty

    var username by usernameProperty

    val passwordProvider: Provider<String>
        get() = passwordProperty

    var password by passwordProperty

    val socketTimeoutProvider: Property<Int>
        get() = socketTimeoutProperty

    var socketTimeout by socketTimeoutProperty

    val requestTimeoutProvider: Property<Int>
        get() = requestTimeoutProperty

    var requestTimeout by requestTimeoutProperty
}