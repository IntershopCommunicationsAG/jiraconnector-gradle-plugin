package com.intershop.gradle.jiraconnector.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException

import javax.inject.Inject
import java.util.regex.Matcher

@CompileStatic
@Slf4j
class SetIssueFieldRunner implements Runnable {

    String baseURL
    String username
    String password

    File issueFile

    String linePattern
    String fieldPattern
    String jiraIssuePattern
    String fieldValue

    String fieldName
    String versionMessage
    boolean mergeMilestoneVersions

    int socketTimeout
    int requestTimeout

    @Inject
    SetIssueFieldRunner(String baseURL, String username, String password, int socketTimeOut, int requestTimeOut,
                        File issueFile, String linePattern, String fieldPattern, String jiraIssuePattern,
                        String fieldName, String fieldValue, String versionMessage, boolean mergeMilestoneVersions) {
        this.baseURL = baseURL
        this.username = username
        this.password = password

        this.issueFile = issueFile

        this.linePattern = linePattern
        this.fieldPattern = fieldPattern
        this.jiraIssuePattern = jiraIssuePattern
        this.fieldValue = fieldValue

        this.fieldName = fieldName
        this.versionMessage = versionMessage
        this.mergeMilestoneVersions = mergeMilestoneVersions

        this.socketTimeout = socketTimeOut
        this.requestTimeout = requestTimeOut
    }

    @Override
    void run() {
        List<String> issueList = JiraIssueParser.parse(getIssueFile(), getLinePattern(), getJiraIssuePattern())

        JiraConnector connector = getPreparedConnector()
        try {
            Matcher fieldMatcher = (getFieldValue() =~ /${getFieldPattern()}/)
            fieldValue = ((List)fieldMatcher[0])[1]
        } catch(Exception ex) {
            log.warn('Fieldvalue {} is used, because field pattern does not work correctly.', fieldValue)
        }

        try {
            connector.processIssues(issueList, fieldName, fieldValue, getVersionMessage(), getMergeMilestoneVersions(), new org.joda.time.DateTime())
        }catch(Exception ex) {
            throw new GradleException("It was not possible to write data to Jira server with '${ex.getMessage()}'")
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
