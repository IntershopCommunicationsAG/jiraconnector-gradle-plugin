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

abstract class JiraConnectorExtension {

    companion object {
        // extension  name
        const val JIRACONNECTOR_EXTENSION_NAME = "jiraConnector"
        // Patternf for Atlassian JIIRA issues
        const val JIRAISSUE_PATTERN = "([A-Z][A-Z0-9]+)-([0-9]+)"

        // default string for messages
        const val JIRAVERSIONMESSAGE = "created by jiraconnector plugin"

        // default atlassian rest client dependencies configuration
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
        versionMessageProperty.convention(JiraConnectorExtension.JIRAVERSIONMESSAGE)
        mergeMilestoneVersionsProperty.convention(true)
    }

    val server: Server = objectFactory.newInstance(Server::class.java)

    val linePatternProvider: Provider<String>
        get() = linePatternProperty

    var linePattern by linePatternProperty

    val fieldNameProvider: Provider<String>
        get() = fieldNameProperty

    var fieldName by fieldNameProperty

    val fieldValueProvider: Provider<String>
        get() = fieldValueProperty

    var fieldValue by fieldValueProperty

    val fieldPatternProvider: Provider<String>
        get() = fieldPatternProperty

    var fieldPattern by fieldPatternProperty

    val versionMessageProvider: Provider<String>
        get() = versionMessageProperty

    var versionMessage by versionMessageProperty

    val mergeMilestoneVersionsProvider: Provider<Boolean>
        get() = mergeMilestoneVersionsProperty

    var mergeMilestoneVersions by mergeMilestoneVersionsProperty

    val issueFileProvider: Provider<RegularFile>
        get() = issueFileProperty

    var issueFile: File
        get() = issueFileProperty.get().asFile
        set(value) = this.issueFileProperty.set(value)

    val replacementsProvider: Provider<MutableMap<String, String>>
        get() = replacementsProperty

    var replacements: Map<String, String>
        get() = replacementsProperty.getOrElse(mapOf<String, String>())
        set(value) = replacementsProperty.set(value)

    fun replacements(key: String, value: String) {
        replacementsProperty.put(key, value)
    }

    fun replacements(map: Map<String, String>) {
        replacementsProperty.putAll(map)
    }

    fun server(closure: Closure<Server>) {
        ConfigureUtil.configure(closure, server)
    }

    fun server(action: Action<in Server>) {
        action.execute(server)
    }
}