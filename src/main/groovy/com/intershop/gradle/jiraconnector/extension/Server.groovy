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

import groovy.util.logging.Slf4j
import org.gradle.api.Project

/**
 * This is the basic configuration needed for JIRA.
 */
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
        baseURL = JiraConnectorExtension.getVariable(project, SERVER_BASEURL_ENV, SERVER_BASEURL_PRJ)
        username = JiraConnectorExtension.getVariable(project, SERVER_USER_NAME_ENV, SERVER_USER_NAME_PRJ)
        password = JiraConnectorExtension.getVariable(project, SERVER_USER_PASSWORD_ENV, SERVER_USER_PASSWORD_PRJ)

        if(! socketTimeout) {
            try {
                socketTimeout = Integer.parseInt(JiraConnectorExtension.getVariable(project, SOCKET_TIMEOUT_ENV, SOCKET_TIMEOUT_PRJ, '3'))
            }catch (NumberFormatException nfe) {
                log.info('Use standard value (3 minutes) for socket timeout')
                socketTimeout = 3
            }
        }

        if(! requestTimeout) {
            try {
                requestTimeout = Integer.parseInt(JiraConnectorExtension.getVariable(project, REQUEST_TIMEOUT_ENV, REQUEST_TIMEOUT_PRJ, '3'))
            }catch (NumberFormatException nfe) {
                log.info('Use standard value (3 minutes) for request timeout')
                requestTimeout = 3
            }
        }
    }

    /**
     * Base URL of the server instance
     */
    String baseURL

    /**
     * Login of the server user
     */
    String username

    /**
     * Password of the server user
     */
    String password

    /**
     * Timeout configuration
     */
    int socketTimeout

    int requestTimeout
}
