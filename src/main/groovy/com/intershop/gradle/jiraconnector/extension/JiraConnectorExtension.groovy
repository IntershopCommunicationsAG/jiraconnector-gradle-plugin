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
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
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
     * Server configuration
     */
    Server server

    /**
     * Line Pattern for Jira Issues
     */
    private final Property<String> linePattern

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
    private final Property<String> fieldName

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
    private final Property<String> fieldValue

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
    private final Property<String> fieldPattern

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

    private final Property<String> versionMessage

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
    private final Property<Boolean> mergeMilestoneVersions

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
    private final RegularFileProperty issueFile

    Provider<RegularFile> getIssueFileProvider() {
        return issueFile
    }

    RegularFile getIssueFile() {
        return issueFile.get()
    }

    void setIssueFile(File issueFile) {
        this.issueFile.set(issueFile)
    }

    /**
     * Map with replacements with component names
     */
    Map<String, String> replacements

    private final MapProperty<String, String> replacements

    MapProperty<String, String> getReplacementsProvider() {
        return replacements
    }

    Map getReplacements() {
        return replacements.get()
    }

    void setReplacements(Map replacements) {
        this.replacements.set(replacements)
    }

    JiraConnectorExtension(Project project) {

        this.project = project
        issueFile = project.objects.fileProperty()

        linePattern = project.objects.property(String)
        fieldName = project.objects.property(String)
        fieldValue = project.objects.property(String)
        fieldPattern = project.objects.property(String)
        versionMessage = project.objects.property(String)
        mergeMilestoneVersions = project.objects.property(Boolean)

        replacements = project.objects.mapProperty(String, String)

        // initialize server configuration
        server = new Server(project)

        setFieldPattern('(.*)')
    }

    Server server(Closure closure) {
        return (Server)project.configure(server, closure)
    }
}
