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

open class JiraField(val field: Field) {

    companion object {
        const val FIXVERSIONS = "fixVersions"
        const val LABELS = "labels"
        const val VERSIONS = "versions"

        val SUPPORTSYSTEMFIELDIDS = listOf(FIXVERSIONS, LABELS, VERSIONS)
    }
    var isSystem = false
    var isString = false
    var isVersion = false
    var isArray = false

    val id: String  = field.id
    val name: String = field.name
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

    val isSupported: Boolean
        get() {
            return (isString || isVersion || isArray || isSystem)
        }
}