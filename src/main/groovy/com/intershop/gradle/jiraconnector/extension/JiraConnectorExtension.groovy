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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

@CompileStatic
@Slf4j
class JiraConnectorExtension {

    private Project project

    // extension  name
    public static final String JIRACONNECTOR_EXTENSION_NAME = 'jiraConnector'
    // Patternf for Atlassian JIIRA issues
    public static final String JIRAISSUE_PATTERN = '([A-Z][A-Z0-9]+)-([0-9]+)'

    // default string for messages
    public static final String JIRAVERSIONMESSAGE = 'created by jiraconnector plugin'

    // default atlassian rest client dependencies configuration
    public static final String JIRARESTCLIENTCONFIGURATION = 'jiraRestClient'

    /**
     * <p>Configuration for the execution on the CI server</p>
     *
     * <p>Can be configured/overwritten with environment variable RUNONCI;
     * java environment RUNONCI or project variable runOnCI</p>
     */
    private final PropertyState<Boolean> runOnCI

    Provider<Boolean> getRunOnCIProvider() {
        return runOnCI
    }

    boolean getRunOnCI() {
        return runOnCI.get().booleanValue()
    }

    void setRunOnCI(boolean runOnCI) {
        this.runOnCI.set(new Boolean(runOnCI))
    }

    /**
     * Server configuration
     */
    Server server

    /**
     * Line Pattern for Jira Issues
     */
    private final PropertyState<String> linePattern

    Provider<String> getLinePatternProvider() {
        return linePattern
    }

    String getLinePattern() {
        return linePattern.get()
    }

    void setLinePattern(String linePattern) {
        this.linePattern.set(linePattern)
    }

    /**
     * Jira field name
     */
    private final PropertyState<String> fieldName

    Provider<String> getFieldNameProvider() {
        return fieldName
    }

    String getFieldName() {
        return fieldName.get()
    }

    void setFieldName(String fieldName) {
        this.fieldName.set(fieldName)
    }

    /**
     *  Jira field value
     */
    private final PropertyState<String> fieldValue

    Provider<String> getFieldValueProvider() {
        return fieldValue
    }

    String getFieldValue() {
        return fieldValue.get()
    }

    void setFieldValue(String fieldValue) {
        this.fieldValue.set(fieldValue)
    }

    /**
     * String pattern for version message field
     */
    private final PropertyState<String> fieldPattern

    Provider<String> getFieldPatternProvider() {
        return fieldPattern
    }

    String getFieldPattern() {
        return fieldPattern.get()
    }

    void setFieldPattern(String fieldPattern) {
        this.fieldPattern.set(fieldPattern)
    }

    /**
     * String value for Jira version editing
     */

    private final PropertyState<String> versionMessage

    Provider<String> getVersionMessageProvider() {
        return versionMessage
    }

    String getVersionMessage() {
        return versionMessage.get()
    }

    void setVersionMessage(String versionMessage) {
        this.versionMessage.set(versionMessage)
    }

    /**
     * Merge previous mile stone versions
     */
    private final PropertyState<Boolean> mergeMilestoneVersions

    Provider<Boolean> getMergeMilestoneVersionsProvider() {
        return mergeMilestoneVersions
    }

    boolean getMergeMilestoneVersions() {
        return mergeMilestoneVersions.get()
    }

    void setMergeMilestoneVersions(boolean mergeMilestoneVersions) {
        this.mergeMilestoneVersions.set(new Boolean(mergeMilestoneVersions))
    }

    /**
     * File with Jira Issue references
     */
    private final PropertyState<File> issueFile

    Provider<File> getIssueFileProvider() {
        return issueFile
    }

    File getIssueFile() {
        return issueFile.get()
    }

    void setIssueFile(File issueFile) {
        this.issueFile.set(issueFile)
    }

    /**
     * Map with replacements with component names
     */
    Map<String, String> replacements

    private final PropertyState<Map<String, String>> replacements

    Provider<Map<String, String>> getReplacementsProvider() {
        return replacements
    }

    Map<String, String> getReplacements() {
        return replacements.get()
    }

    void setReplacements(Map<String, String> replacements) {
        this.replacements.set(replacements)
    }

    JiraConnectorExtension(Project project) {

        this.project = project

        runOnCI = project.property(Boolean)

        issueFile = project.property(File)

        linePattern = project.property(String)
        fieldName = project.property(String)
        fieldValue = project.property(String)
        fieldPattern = project.property(String)
        versionMessage = project.property(String)
        mergeMilestoneVersions = project.property(Boolean)

        replacements = project.property(Map)

        // initialize server configuration
        server = new Server(project)

        setFieldPattern('(.*)')
    }

    Server server(Closure closure) {
        return (Server)project.configure(server, closure)
    }
}
