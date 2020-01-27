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
package com.intershop.gradle.jiraconnector.task.jira

import com.atlassian.jira.rest.client.api.domain.Field

/**
 * Jira field implementaion of this plugin.
 *
 * @constructor creates a class based on the original Rest Jira field.
 * @property field  Jira Field from Rest client.
 */
open class JiraField(val field: Field) {

    companion object {
        /**
         * Jira system field name for fixed versions.
         */
        const val FIXVERSIONS = "fixVersions"

        /**
         * Jira system field name for labels.
         */
        const val LABELS = "labels"

        /**
         * Jira system field name for versions.
         */
        const val VERSIONS = "versions"

        /**
         * List of supported Jira system fields.
         */
        val SUPPORTSYSTEMFIELDIDS = listOf(FIXVERSIONS, LABELS, VERSIONS)
    }

    /**
     * Field id of the original JIRA field.
     *
     * @property id
     */
    val id: String  = field.id

    /**
     * Field name of the original JIRA field.
     *
     * @property name
     */
    val name: String = field.name

    /**
     * This is true, if the field is a system field.
     *
     * @property isSystem
     */
    var isSystem = false

    /**
     * This is true, if the field is a single string field.
     *
     * @property isString
     */
    var isString = false

    /**
     * This is true, if the field is a version field.
     * Values are listed in a version list.
     *
     * @property isVersion
     */
    var isVersion = false

    /**
     * This is true, if the field is array field.
     *
     * @property isArray
     */
    var isArray = false

    /**
     * This property stores the system value,
     * if the field is a system field.
     */
    var system: String? = null

    init {
        if(field.schema != null) {
            isSystem = SUPPORTSYSTEMFIELDIDS.contains(field.getSchema()?.getSystem())
            if(isSystem) {
                system = field.getSchema()?.getSystem()
            } else {
                system = null
                isArray = (field.getSchema()?.getType() == "array")

                if(isArray) {
                    isString = (field.getSchema()?.getItems() == "string")
                    isVersion = (field.getSchema()?.getItems() == "version")
                } else {
                    isString = (field.getSchema()?.getType() == "string")
                    isVersion = (field.getSchema()?.getType() == "version")
                }
            }
        }
    }

    /**
     * This property returns true, if the field is supported.
     *
     * @property isSupported
     */
    val isSupported: Boolean
        get() {
            return (isString || isVersion || isArray || isSystem)
        }
}
