package com.intershop.gradle.jiraconnector

import com.intershop.gradle.test.AbstractIntegrationGroovySpec
import com.intershop.gradle.test.AbstractIntegrationSpec
import com.intershop.gradle.test.AbstractProjectSpec
import groovy.util.logging.Slf4j
import spock.lang.Requires

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Slf4j
class JiraIntegrationTest extends AbstractIntegrationGroovySpec {

    def 'test with real changelog - #gradleVersion'() {
        setup:
        File settingsFile = new File(testProjectDir, 'settings.gradle')
        settingsFile << """
            // define root proejct name
            rootProject.name = 'commerce_management_b2c'
            """.stripIndent()

        copyResources("assemblychangelog.asciidoc", "build/assemblychangelog.asciidoc")

        buildFile << """
            plugins {
                id 'com.intershop.gradle.jiraconnector'
            }
            
            task(setIsseuFieldForAssembly, type: com.intershop.gradle.jiraconnector.task.SetIssueField) {

                baseURL = "https://jira.intershop.de"
                username = "mraab"
                password = "muenchenroda4"
            
                linePattern = '3\\\\+.*'
                fieldName = 'Fix Version/s'
                versionMessage = 'version created by build plugin'
                issueFile = file('build/assemblychangelog.asciidoc')
                jiraIssuePattern = '([A-Z][A-Z0-9]+)-([0-9]+)'
                fieldPattern = '[a-z1-9_]*_(.*)'
                fieldValue = "commerce_management_b2c/7.10.12.4"
                mergeMilestoneVersions = true
                requestTimeout = 300
                socketTimeout = 300
            }

            repositories {
                jcenter()
            }
        """.stripIndent()

        when:
        def result = getPreparedGradleRunner()
                .withArguments('setIsseuFieldForAssembly', '-s')
                .build()
        then:
        result.task(':setIsseuFieldForAssembly').outcome == SUCCESS
    }

}
