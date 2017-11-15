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
package com.intershop.gradle.jiraconnector

import com.intershop.gradle.jiraconnector.util.JiraConnector
import com.intershop.gradle.jiraconnector.util.JiraField
import com.intershop.gradle.jiraconnector.util.JiraTestValues
import com.intershop.gradle.jiraconnector.util.TestDispatcher
import com.intershop.gradle.test.AbstractIntegrationSpec
import com.squareup.okhttp.mockwebserver.MockWebServer
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.junit.Rule
import spock.lang.Requires
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Slf4j
@Unroll
class IntegrationSpec extends AbstractIntegrationSpec {

    @Rule
    public final MockWebServer server = new MockWebServer()

    JsonSlurper jsonSlurper = new JsonSlurper()
    def 'test base functionality - mock - #gradleVersion'(gradleVersion) {
        setup:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'

        buildFile << """
            plugins {
                id 'com.intershop.gradle.jiraconnector'
            }

            version = '10.0.6'

            task createFile {
                ext.destFile = new File(buildDir, 'changelog/changelog.asciidoc')
                outputs.file destFile
                doLast {
                    destFile.parentFile.mkdirs()

                    destFile.append(\"""
                    = Change Log for 2.0.0

                    This list contains changes since version 1.0.0. +
                    Created: Sun Feb 21 17:11:48 CET 2016

                    [cols="5%,5%,90%", width="95%", options="header"]
                    |===
                    3+| ${JiraTestValues.issueKey} change on master (e6c62c43)
                    | | M |  gradle.properties
                    3+| remove unnecessary files (a2da48ad)
                    | | D | gradle/wrapper/gradle-wrapper.jar
                    | | D | gradle/wrapper/gradle-wrapper.properties
                    |===\""".stripIndent())
                }
            }

            jiraConnector {
                server {
                    baseURL = '${hostUrlStr}'
                    username = 'test'
                    password = 'test'
                }

                linePattern = '3\\\\+.*'
                fieldName = 'Labels'
                fieldValue = "\${project.name}/\${project.getVersion()}"
            }

            jiraConnector.issueFile = tasks.createFile.outputs.files.singleFile
            
            tasks.toArray().each {
                println it.name
            }
            
            tasks.setIssueField.dependsOn tasks.findByName('createFile')

            repositories {
                jcenter()
            }
        """.stripIndent()

        File settingsFile = new File(testProjectDir, 'settings.gradle')
        settingsFile << """
            // define root proejct name
            rootProject.name = 'p_platform'
            """.stripIndent()

        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'emptyLabels.response'))
        def result = getPreparedGradleRunner()
                .withArguments('setIssueField', '--stacktrace', '-i', '-PrunOnCI=true')
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.task(':createFile').outcome == SUCCESS
        result.task(':setIssueField').outcome == SUCCESS
        (new File(testProjectDir, 'build/changelog/changelog.asciidoc')).exists()
        ! result.output.contains("Project variable 'projectKey' is missing!")
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"labels":["p_platform\\/10.0.6"]}}'))

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'test functionality with environment configuration - mock - #gradleVersion'(gradleVersion) {
        setup:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'

        buildFile << """
            plugins {
                id 'com.intershop.gradle.jiraconnector'
            }

            version = '10.0.6'

            task createFile {
                ext.destFile = new File(buildDir, 'changelog/changelog.asciidoc')
                outputs.file destFile
                doLast {
                    destFile.parentFile.mkdirs()

                    destFile.append(\"""
                    = Change Log for 2.0.0

                    This list contains changes since version 1.0.0. +
                    Created: Sun Feb 21 17:11:48 CET 2016

                    [cols="5%,5%,90%", width="95%", options="header"]
                    |===
                    3+| ${JiraTestValues.issueKey} change on master (e6c62c43)
                    | | M |  gradle.properties
                    3+| remove unnecessary files (a2da48ad)
                    | | D | gradle/wrapper/gradle-wrapper.jar
                    | | D | gradle/wrapper/gradle-wrapper.properties
                    |===\""".stripIndent())
                }
            }

            jiraConnector {
                linePattern = '3\\\\+.*'
                fieldName = 'Labels'
                fieldValue = "\${project.name}/\${project.getVersion()}"
            }

            jiraConnector.issueFile = tasks.createFile.outputs.files.singleFile
            tasks.setIssueField.dependsOn tasks.findByName('createFile')

            repositories {
                jcenter()
            }
        """.stripIndent()

        File settingsFile = new File(testProjectDir, 'settings.gradle')
        settingsFile << """
            // define root proejct name
            rootProject.name = 'p_platform'
            """.stripIndent()

        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'emptyLabels.response'))
        def result = getPreparedGradleRunner()
                .withArguments('setIssueField', '--stacktrace', '-i', "-PjiraBaseURL=${hostUrlStr}", '-PjiraUserName=test', '-PjiraUserPASSWD=test', '-PrunOnCI=true')
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.task(':createFile').outcome == SUCCESS
        result.task(':setIssueField').outcome == SUCCESS
        (new File(testProjectDir, 'build/changelog/changelog.asciidoc')).exists()
        ! result.output.contains("Project variable 'projectKey' is missing!")
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"labels":["p_platform\\/10.0.6"]}}'))

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'test base functionality with wrong pattern - mock - #gradleVersion'(gradleVersion) {
        setup:
        String urlStr = server.url('rest/api/latest').toString()
        String hostUrlStr = urlStr - '/rest/api/latest'

        buildFile << """
            plugins {
                id 'com.intershop.gradle.jiraconnector'
            }

            version = '10.0.6'

            task createFile {
                ext.destFile = new File(buildDir, 'changelog/changelog.asciidoc')
                outputs.file destFile
                doLast {
                    destFile.parentFile.mkdirs()

                    destFile.append(\"""
                    = Change Log for 2.0.0

                    This list contains changes since version 1.0.0. +
                    Created: Sun Feb 21 17:11:48 CET 2016

                    [cols="5%,5%,90%", width="95%", options="header"]
                    |===
                    3+| ${JiraTestValues.issueKey} change on master (e6c62c43)
                    | | M |  gradle.properties
                    3+| remove unnecessary files (a2da48ad)
                    | | D | gradle/wrapper/gradle-wrapper.jar
                    | | D | gradle/wrapper/gradle-wrapper.properties
                    |===\""".stripIndent())
                }
            }

            jiraConnector {
                server {
                    baseURL = '${hostUrlStr}'
                    username = 'test'
                    password = 'test'
                }

                linePattern = '3\\\\+.*'
                fieldName = 'Labels'
                fieldValue = "\${project.name}/\${project.getVersion()}"
                fieldPattern = '[a-z1-9]*_(.*)'
            }

            jiraConnector.issueFile = tasks.createFile.outputs.files.singleFile
            tasks.setIssueField.dependsOn tasks.findByName('createFile')

            repositories {
                jcenter()
            }
        """.stripIndent()

        File settingsFile = new File(testProjectDir, 'settings.gradle')
        settingsFile << """
            // define root proejct name
            rootProject.name = 'platform'
            """.stripIndent()

        Map requestsBodys = [:]

        when:
        server.setDispatcher(TestDispatcher.getProcessLabelTestDispatcher(requestsBodys, 'emptyLabels.response'))
        def result = getPreparedGradleRunner()
                .withArguments('setIssueField', '--stacktrace', '-i', '-PrunOnCI=true')
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result.task(':createFile').outcome == SUCCESS
        result.task(':setIssueField').outcome == SUCCESS
        result.output.contains('Fieldvalue platform/10.0.6 is used, because field pattern does not work correctly.')
        (new File(testProjectDir, 'build/changelog/changelog.asciidoc')).exists()
        ! result.output.contains("Project variable 'projectKey' is missing!")
        jsonSlurper.parseText(requestsBodys.get('onebody')).equals(jsonSlurper.parseText('{"fields":{"project":{"key":"ISTOOLS"},"issuetype":{"id":"10001"},"labels":["platform\\/10.0.6"]}}'))

        where:
        gradleVersion << supportedGradleVersions
    }

    @Requires({
        System.properties['jira_url_config'] &&
                System.properties['jira_user_config'] &&
                System.properties['jira_passwd_config']
    })
    def 'test correct version list - #gradleVersion'(gradleVersion) {
        setup:
        buildFile << """
            plugins {
                id 'com.intershop.gradle.jiraconnector'
            }

            jiraConnector {
                server {
                    baseURL = '${System.properties['jira_url_config']}'
                    username = '${System.properties['jira_user_config']}'
                    password = '${System.properties['jira_passwd_config']}'
                }
                replacements = ['p_wa':'wa']
            }
            repositories {
                jcenter()
            }
        """.stripIndent()

        when:
        def result = getPreparedGradleRunner()
                .withArguments('correctVersionList', '-PprojectKey=IS', '--stacktrace', '-i', '-PrunOnCI=true', '-s')
                .withGradleVersion(gradleVersion)
                .build()
        then:
        result.task(':correctVersionList').outcome == SUCCESS

        where:
        gradleVersion << supportedGradleVersions
    }
}
