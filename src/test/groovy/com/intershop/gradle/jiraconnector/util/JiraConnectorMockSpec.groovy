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


package com.intershop.gradle.jiraconnector.util

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.squareup.okhttp.mockwebserver.MockWebServer
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.junit.Rule
import spock.lang.Specification

class JiraConnectorMockSpec extends Specification {

    @Rule
    public final MockWebServer server = new MockWebServer()

    JsonSlurper jsonSlurper = new JsonSlurper()

    def 'can add one label to an issue for labels - mock'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'emptyLabels.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.LABELSNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"labels":["platform\\/10.0.6"]}}'))

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'oneversionLabels.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.LABELSNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())

        then:
        Map result = jsonSlurper.parseText(requestsBodys.get('onebody'))
        result.fields.get('labels').sort() == (['platform/10.0.7', 'platform/10.0.6'] as Object[]).sort()
    }

    def 'can add one label to an issue for custom field - mock'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'emptyCustomLabels.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.BUILDVERSIONSNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"customfield_12190":["platform\\/10.0.6"]}}'))

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'oneversionCustomLabels.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.BUILDVERSIONSNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())

        then:
        Map result = jsonSlurper.parseText(requestsBodys.get('onebody'))
        result.fields.get('customfield_12190').sort() == (['platform/10.0.7', 'platform/10.0.6'] as Object[]).sort()
    }

    def 'can add a fix version to an issue - mock'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'emptyFixVersion.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.FIXVERSIONNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"fixVersions":[{"name":"platform\\/10.0.6"}]}}'))

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'oneFixVersion.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.FIXVERSIONNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())

        then:
        Map result = jsonSlurper.parseText(requestsBodys.get('onebody'))
        result.fields.get('fixVersions').sort() == ([[name: 'platform/10.0.7'], [name: 'platform/10.0.6']] as Object[]).sort()
    }

    def 'can add a affected version to an issue - mock'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'emptyAffectedVersion.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.AFFECTEDVERSIONAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"versions":[{"name":"platform\\/10.0.6"}],"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"}}}'))

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'oneAffectedVersion.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.AFFECTEDVERSIONAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())

        then:
        Map result = jsonSlurper.parseText(requestsBodys.get('onebody'))
        result.fields.get('versions').sort() == ([[name: 'platform/10.0.7'], [name: 'platform/10.0.6']] as Object[]).sort()
    }

    def 'can add a version to a custom array field of an issue - mock'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'emptyVersionArrayCustomField.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.MULTITESTVERSIONAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"customfield_12290":[{"name":"platform\\/10.0.6"}]}}'))

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'oneVersionArrayCustomField.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.MULTITESTVERSIONAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())

        then:
        Map result = jsonSlurper.parseText(requestsBodys.get('onebody'))
        result.fields.get('customfield_12290').sort() == ([[name: 'platform/10.0.7'], [name: 'platform/10.0.6']] as Object[]).sort()
    }

    def 'can add a version to a custom field of an issue - mock'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'emptyVersionCustomField.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.TESTEDVERSIONNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"customfield_10891":{"name":"platform\\/10.0.6"},"issuetype":{"id":"10001"}}}'))

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'firstOneVersionCustomField.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.TESTEDVERSIONNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())

        then:
        Map result = jsonSlurper.parseText(requestsBodys.get('onebody'))
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"customfield_10891":{"name":"platform\\/10.0.7"},"issuetype":{"id":"10001"}}}'))

    }

    def 'fieldname does not exists'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessVersionTestDispatcher(requestsBodys, 'emptyVersionCustomField.response'))
        jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.UNKNOWNFIELD, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        InvalidFieldnameException ex = thrown()
        ex.message == "Field '${JiraTestValues.UNKNOWNFIELD}' is not available!"
    }

    def 'Issue does not exists'() {
        given:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'
        JiraConnector jiraConnector = new JiraConnector(hostUrlStr,'test','test')
        JiraRestClient restClient = jiraConnector.getClient()
        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'oneversionLabels.response'))
        jiraConnector.processIssues([JiraTestValues.unknownIssue], JiraTestValues.LABELSNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
        notThrown Exception
    }
}
