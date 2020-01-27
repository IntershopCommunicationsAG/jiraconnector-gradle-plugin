/*
 * Copyright 2019 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intershop.gradle.jiraconnector.extension

import com.intershop.gradle.jiraconnector.util.getValue
import com.intershop.gradle.jiraconnector.util.setValue
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.util.ConfigureUtil
import java.io.File
import javax.inject.Inject

/**
 * Main extension of Jira Connector Plugin.
 */
abstract class JiraConnectorExtension {

    companion object {
        /**
         * Name of the extension.
         */
        const val JIRACONNECTOR_EXTENSION_NAME = "jiraConnector"

        /**
         * Regex pattern of Atlassian JIIRA issues.
         */
        const val JIRAISSUE_PATTERN = "([A-Z][A-Z0-9]+)-([0-9]+)"

        /**
         * Default message for changes in Jira.
         */
        const val JIRAVERSIONMESSAGE = "created by jiraconnector plugin"

        /**
         * Name of the Gradle configuration with dependencies
         * of the currect rest Jira restclient.
         */
        const val JIRARESTCLIENTCONFIGURATION = "jiraRestClient" 
    }

    /**
     * Inject service of ObjectFactory (See "Service injection" in Gradle documentation.
     */
    @get:Inject
    abstract val objectFactory: ObjectFactory

    private val linePatternProperty: Property<String> = objectFactory.property(String::class.java)
    private val fieldNameProperty: Property<String> = objectFactory.property(String::class.java)
    private val fieldValueProperty: Property<String> = objectFactory.property(String::class.java)
    private val fieldPatternProperty: Property<String> = objectFactory.property(String::class.java)
    private val versionMessageProperty: Property<String> = objectFactory.property(String::class.java)
    private val mergeMilestoneVersionsProperty: Property<Boolean> = objectFactory.property(Boolean::class.java)
    private val issueFileProperty: RegularFileProperty = objectFactory.fileProperty()
    private val replacementsProperty: MapProperty<String, String> =
            objectFactory.mapProperty(String::class.java, String::class.java)

    init {
        fieldPatternProperty.set("(.*)")
        versionMessageProperty.convention(JIRAVERSIONMESSAGE)
        mergeMilestoneVersionsProperty.convention(true)
    }

    /**
     * Connection configuration of Jira server.
     *
     * @property server
     */
    val server: Server = objectFactory.newInstance(Server::class.java)

    /**
     * Provider for the linepattern property.
     *
     * @property linePatternProvider
     */
    val linePatternProvider: Provider<String>
        get() = linePatternProperty

    /**
     * Line pattern for the search of Jira issues.
     *
     * @property linePattern
     */
    var linePattern by linePatternProperty

    /**
     * Provider for the field name property.
     *
     * @property fieldNameProvider
     */
    val fieldNameProvider: Provider<String>
        get() = fieldNameProperty

    /**
     * Field name for the change of the Jira issues.
     *
     * @property fieldName
     */
    var fieldName by fieldNameProperty

    /**
     * Provider for the field value property.
     *
     * @property fieldValueProvider
     */
    val fieldValueProvider: Provider<String>
        get() = fieldValueProperty

    /**
     * Field value for the change of the Jira issues.
     *
     * @property fieldName
     */
    var fieldValue by fieldValueProperty

    /**
     * Provider for the field pattern property.
     *
     * @property fieldPatternProvider
     */
    val fieldPatternProvider: Provider<String>
        get() = fieldPatternProperty

    /**
     * Field pattern for the change of the Jira issues.
     *
     * @property fieldPattern
     */
    var fieldPattern by fieldPatternProperty

    /**
     * Provider for the version message property.
     *
     * @property versionMessageProvider
     */
    val versionMessageProvider: Provider<String>
        get() = versionMessageProperty

    /**
     * Version message for the change of the Jira issues.
     *
     * @property versionMessage
     */
    var versionMessage by versionMessageProperty

    /**
     * Provider for the merge milestone property.
     *
     * @property mergeMilestoneVersionsProvider
     */
    val mergeMilestoneVersionsProvider: Provider<Boolean>
        get() = mergeMilestoneVersionsProperty

    /**
     * Milestones will be merged to one new version
     * if this property is true.
     *
     * @property mergeMilestoneVersions
     */
    var mergeMilestoneVersions by mergeMilestoneVersionsProperty

    /**
     * Provider for the issue file property.
     *
     * @property issueFileProvider
     */
    val issueFileProvider: Provider<RegularFile>
        get() = issueFileProperty

    /**
     * Property for file with Jira issues.
     *
     * @property issueFile
     */
    var issueFile: File
        get() = issueFileProperty.get().asFile
        set(value) = this.issueFileProperty.set(value)

    /**
     * Provider for the replacements property.
     *
     * @property replacementsProvider
     */
    val replacementsProvider: Provider<MutableMap<String, String>>
        get() = replacementsProperty

    /**
     * Property with replacements for editing issue.
     *
     * @property replacements:
     */
    var replacements: Map<String, String>
        get() = replacementsProperty.getOrElse(mapOf<String, String>())
        set(value) = replacementsProperty.set(value)

    /**
     * Add entries to the replacements.
     *
     * @param key   will be replaced
     * @param value with
     */
    fun replacements(key: String, value: String) {
        replacementsProperty.put(key, value)
    }

    /**
     * Add an map to the replacements.
     *
     * @param map
     */
    fun replacements(map: Map<String, String>) {
        replacementsProperty.putAll(map)
    }

    /**
     * Configures a server from a closure.
     *
     * @param closure with server configurtion
     */
    fun server(closure: Closure<Server>) {
        ConfigureUtil.configure(closure, server)
    }

    /**
     * Configures a server from a action.
     *
     * @param action with server configurtion
     */
    fun server(action: Action<in Server>) {
        action.execute(server)
    }
}
