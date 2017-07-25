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
package com.intershop.gradle.jiraconnector.extension

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

/**
 * This is the basic configuration needed for JIRA.
 */
@CompileStatic
@Slf4j
class Server {

    private Project project

    Server(Project project) {
        baseURL = project.property(String)
        username = project.property(String)
        password = project.property(String)

        socketTimeout = project.property(Integer)
        requestTimeout = project.property(Integer)
    }

    /**
     * Base URL of the server instance
     */
    private final PropertyState<String> baseURL

    Provider<String> getBaseURLProvider() {
        return baseURL
    }

    String getBaseURL() {
        return baseURL.get()
    }

    void setBaseURL(String baseURL) {
        this.baseURL.set(baseURL)
    }

    /**
     * Login of the server user
     */
    private final PropertyState<String> username

    Provider<String> getUsernameProvider() {
        username
    }

    String getUsername() {
        return username.get()
    }

    void setUsername(String username) {
        this.username.set(username)
    }

    /**
     * Password of the server user
     */
    private final PropertyState<String> password

    Provider<String> getPasswordProvider() {
        password
    }

    String getPassword() {
        return password.get()
    }

    void setPassword(String password) {
        this.password.set(password)
    }

    /**
     * Timeout configuration
     */
    private final PropertyState<Integer> socketTimeout

    Provider<Integer> getSocketTimeoutProvider() {
        return socketTimeout
    }

    int getSocketTimeout() {
        return socketTimeout.get().intValue()
    }

    void setSocketTimeout(int socketTimeout) {
        this.socketTimeout.set(new Integer(socketTimeout))
    }

    private final PropertyState<Integer> requestTimeout

    Provider<Integer> getRequestTimeoutProvider() {
        return requestTimeout
    }

    int getRequestTimeout() {
        return requestTimeout.get().intValue()
    }

    void setRequestTimeout(int requestTimeout) {
        this.requestTimeout.set(new Integer(requestTimeout))
    }
}
