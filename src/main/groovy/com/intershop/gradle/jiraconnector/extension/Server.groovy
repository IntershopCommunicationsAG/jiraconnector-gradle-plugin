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

    public final static String SERVER_USER_NAME_ENV = 'JIRAUSERNAME'
    public final static String SERVER_USER_NAME_PRJ = 'jiraUserName'

    public final static String SERVER_USER_PASSWORD_ENV = 'JIRAUSERPASSWD'
    public final static String SERVER_USER_PASSWORD_PRJ = 'jiraUserPASSWD'

    public final static String SERVER_BASEURL_ENV = 'JIRABASEURL'
    public final static String SERVER_BASEURL_PRJ = 'jiraBaseURL'

    // time out configuration
    public final static String SOCKET_TIMEOUT_ENV = 'SOCKET_TIMEOUT'
    public final static String SOCKET_TIMEOUT_PRJ = 'socketTimeout'

    public final static String REQUEST_TIMEOUT_ENV = 'REQUEST_TIMEOUT'
    public final static String REQUEST_TIMEOUT_PRJ = 'requestTimeout'

    Server(Project project) {
        baseURL.set(JiraConnectorExtension.getVariable(project, SERVER_BASEURL_ENV, SERVER_BASEURL_PRJ))
        username.set(JiraConnectorExtension.getVariable(project, SERVER_USER_NAME_ENV, SERVER_USER_NAME_PRJ))
        password.set(JiraConnectorExtension.getVariable(project, SERVER_USER_PASSWORD_ENV, SERVER_USER_PASSWORD_PRJ))

        if(! socketTimeout) {
            try {
                socketTimeout.set(new Integer(JiraConnectorExtension.getVariable(project, SOCKET_TIMEOUT_ENV, SOCKET_TIMEOUT_PRJ, '3')))
            }catch (NumberFormatException nfe) {
                log.info('Use standard value (3 minutes) for socket timeout')
                socketTimeout.set(new Integer(3))
            }
        }

        if(! requestTimeout) {
            try {
                requestTimeout.set(new Integer(JiraConnectorExtension.getVariable(project, REQUEST_TIMEOUT_ENV, REQUEST_TIMEOUT_PRJ, '3')))
            }catch (NumberFormatException nfe) {
                log.info('Use standard value (3 minutes) for request timeout')
                requestTimeout.set(new Integer(3))
            }
        }
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
