package com.intershop.gradle.jiraconnector.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.inject.Inject

@CompileStatic
@Slf4j
class CorrectVersionListRunner implements Runnable {

    String baseURL
    String username
    String password

    String projectKey
    Map replacements

    int socketTimeout
    int requestTimeout

    @Inject
    CorrectVersionListRunner(String baseURL, String username, String password,
                             int socketTimeOut, int requestTimeOut, String projectKey,
                             Map replacements) {
        this.baseURL = baseURL
        this.username = username
        this.password = password

        this.socketTimeout = socketTimeOut
        this.requestTimeout = requestTimeOut

        this.projectKey = projectKey
        this.replacements = replacements
    }

    @Override
    void run() {
        JiraConnector connector = getPreparedConnector()
        connector.sortVersions(projectKey)
        if(getReplacements()) {
            connector.fixVersionNames(projectKey, getReplacements())
        }
    }

    private JiraConnector getPreparedConnector() {
        if(getBaseURL() && getUsername() && getPassword()) {
            JiraConnector connector = new JiraConnector(getBaseURL(), getUsername(), getPassword())

            if(getSocketTimeout()) {
                connector.setSocketTimeout(getSocketTimeout())
            }
            if(getRequestTimeout()) {
                connector.setRequestTimeout(getRequestTimeout())
            }

            return connector
        }
        return null
    }
}
