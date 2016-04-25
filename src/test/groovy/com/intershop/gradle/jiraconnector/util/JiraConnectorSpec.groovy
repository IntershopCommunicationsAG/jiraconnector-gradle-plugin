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
import com.atlassian.jira.rest.client.api.MetadataRestClient
import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import org.joda.time.DateTime
import spock.lang.Requires
import spock.lang.Specification

@Requires({ System.properties['jira_url_config'] &&
        System.properties['jira_user_config'] &&
        System.properties['jira_passwd_config'] })
class JiraConnectorSpec extends Specification {

    JiraConnector jiraConnector

    def setup() {
        jiraConnector = new JiraConnector(System.properties['jira_url_config'],
                                          System.properties['jira_user_config'],
                                          System.properties['jira_passwd_config'])
    }

    def 'can add one label to an issue for labels'() {
        com.atlassian.jira.rest.client.api.domain.Issue issue
        given:
            JiraRestClient restClient = jiraConnector.getClient()
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()
            IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(JiraField.LABELS, []).build()
            restClient.getIssueClient().updateIssue(issue.key, input).claim()

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.LABELSNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getLabels().contains(JiraTestValues.versionStr)

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.LABELSNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getLabels().contains(JiraTestValues.versionStr) && issue.getLabels().contains(JiraTestValues.addVersionStr)

        cleanup:
            jiraConnector.destroyClient(restClient)
    }

    def 'can add one label to an issue for custom field'() {
        com.atlassian.jira.rest.client.api.domain.Issue issue
        given:
            JiraRestClient restClient = jiraConnector.getClient()
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

            MetadataRestClient mrClient = restClient.metadataClient
            Iterable<Field> fields = mrClient.getFields().claim()
            Field field = (Field)fields.find { ((Field)it).getName() == JiraTestValues.BUILDVERSIONSNAME }

            IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(field.getId(), []).build()
            restClient.getIssueClient().updateIssue(issue.key, input).claim()

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.BUILDVERSIONSNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getFieldByName(JiraTestValues.BUILDVERSIONSNAME).getValue().toString().replace('\\','').contains(JiraTestValues.versionStr)

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.BUILDVERSIONSNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getFieldByName(JiraTestValues.BUILDVERSIONSNAME).getValue().toString().replace('\\','').contains(JiraTestValues.versionStr) &&
                    issue.getFieldByName(JiraTestValues.BUILDVERSIONSNAME).getValue().toString().replace('\\','').contains(JiraTestValues.addVersionStr)

        cleanup:
            jiraConnector.destroyClient(restClient)
    }

    def 'can add a fix version to an issue'() {
        com.atlassian.jira.rest.client.api.domain.Issue issue
        given:
            JiraRestClient restClient = jiraConnector.getClient()
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()
            IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFixVersions([]).build()
            restClient.getIssueClient().updateIssue(issue.key, input).claim()


        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.FIXVERSIONNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getFixVersions().toString().contains(JiraTestValues.versionStr)

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.FIXVERSIONNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getFixVersions().toString().contains(JiraTestValues.versionStr) &&
                    issue.getFixVersions().toString().contains(JiraTestValues.addVersionStr)

        cleanup:
            jiraConnector.destroyClient(restClient)
    }

    def 'can add a affected version to an issue'() {
        com.atlassian.jira.rest.client.api.domain.Issue issue
        given:
            JiraRestClient restClient = jiraConnector.getClient()
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()
            IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setAffectedVersions([]).build()
            restClient.getIssueClient().updateIssue(issue.key, input).claim()

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.AFFECTEDVERSIONAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getAffectedVersions().toString().contains(JiraTestValues.versionStr)

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.AFFECTEDVERSIONAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getAffectedVersions().toString().contains(JiraTestValues.versionStr) &&
                    issue.getAffectedVersions().toString().contains(JiraTestValues.addVersionStr)

        cleanup:
            jiraConnector.destroyClient(restClient)
    }

    def 'can add a version to a custom array field of an issue'() {
        com.atlassian.jira.rest.client.api.domain.Issue issue
        given:
            JiraRestClient restClient = jiraConnector.getClient()
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

            MetadataRestClient mrClient = restClient.metadataClient
            Iterable<Field> fields = mrClient.getFields().claim()
            Field field = (Field)fields.find { ((Field)it).getName() == JiraTestValues.MULTITESTVERSIONAME }

            IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(field.getId(), []).build()
            restClient.getIssueClient().updateIssue(issue.key, input).claim()


        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.MULTITESTVERSIONAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
        issue.getAffectedVersions().toString().replace('\\','').contains(JiraTestValues.versionStr)

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.MULTITESTVERSIONAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getFieldByName(JiraTestValues.MULTITESTVERSIONAME).getValue().toString().replace('\\','').contains(JiraTestValues.versionStr) &&
                issue.getFieldByName(JiraTestValues.MULTITESTVERSIONAME).getValue().toString().replace('\\','').contains(JiraTestValues.addVersionStr)

        cleanup:
            jiraConnector.destroyClient(restClient)

    }

    def 'can add a version to a custom field of an issue'() {
        com.atlassian.jira.rest.client.api.domain.Issue issue
        given:
            JiraRestClient restClient = jiraConnector.getClient()
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

            MetadataRestClient mrClient = restClient.metadataClient
            Iterable<Field> fields = mrClient.getFields().claim()
            Field field = (Field)fields.find { ((Field)it).getName() == JiraTestValues.TESTEDVERSIONNAME }

            IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(field.getId(), null).build()
            restClient.getIssueClient().updateIssue(issue.key, input).claim()

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.TESTEDVERSIONNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            issue.getFieldByName(JiraTestValues.TESTEDVERSIONNAME).getValue().toString().replace('\\','').contains(JiraTestValues.versionStr)

        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.TESTEDVERSIONNAME, JiraTestValues.addVersionStr, JiraTestValues.message, true, DateTime.now())
            issue = restClient.issueClient.getIssue(JiraTestValues.issueKey).claim()

        then:
            ! issue.getFieldByName(JiraTestValues.TESTEDVERSIONNAME).getValue().toString().replace('\\','').contains(JiraTestValues.versionStr) &&
                issue.getFieldByName(JiraTestValues.TESTEDVERSIONNAME).getValue().toString().replace('\\','').contains(JiraTestValues.addVersionStr)

        cleanup:
            jiraConnector.destroyClient(restClient)
    }

    def 'fieldname does not exists'() {
        when:
            jiraConnector.processIssues([JiraTestValues.issueKey], JiraTestValues.UNKNOWNFIELD, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
            InvalidFieldnameException ex = thrown()
            ex.message == "Field '${JiraTestValues.UNKNOWNFIELD}' is not available!"
    }

    def 'Issue does not exists'() {
        when:
            jiraConnector.processIssues([JiraTestValues.unknownIssue], JiraTestValues.LABELSNAME, JiraTestValues.versionStr, JiraTestValues.message, true, DateTime.now())

        then:
            notThrown Exception
    }
}
