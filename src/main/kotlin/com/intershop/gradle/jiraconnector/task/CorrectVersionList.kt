/*
 * Copyright 2020 Intershop Communications AG.
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
package com.intershop.gradle.jiraconnector.task

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task that updates a version list of a Jira project.
 *
 * @constructor creates a class with an injected workerexecutor.
 */
abstract class CorrectVersionList @Inject constructor(private val workerExecutor: WorkerExecutor) : JiraConnectTask() {

    private val replacementsProperty: MapProperty<String, String> =
            objectFactory.mapProperty(String::class.java, String::class.java)
    private val jiraProjectKeyProperty: Property<String> = objectFactory.property(String::class.java)

    init {
        jiraProjectKeyProperty.convention("")
        description = "This task sorts the version of the selected project ('--jiraProjectKey=<key>')"
    }

    /**
     * Replacements are used during the task execution.
     *
     * @property replacements
     */
    @get:Input
    var replacements: Map<String, String>
        get() = replacementsProperty.get()
        set(value) = replacementsProperty.set(value)

    /**
     * Add provider for replacements property.
     *
     * @param replacements
     */
    fun provideReplacements(replacements: Provider<MutableMap<String, String>>) = replacementsProperty.set(replacements)

    /**
     * Property for the used Jira project.
     * This value must be specified.
     */
    @set:Option(option = "jiraProjectKey", description = "This project key is used to sort the versions.")
    @get:Input
    var jiraProjectKey: String
        get() = jiraProjectKeyProperty.get()
        set(value) = jiraProjectKeyProperty.set(value)

    /**
     * Implementation of the task action.
     */
    @TaskAction
    fun correctVersionListAction() {
        if(jiraProjectKeyProperty.get().isEmpty()) {
            throw GradleException("Please specify an project on the command line. Use  --jiraProjectKey.")
        }

        // start runner
        val workQueue = workerExecutor.classLoaderIsolation {
            it.classpath.from(project.configurations.
                    findByName(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION)?.files)
        }

        workQueue.submit(CorrectVersionListRunner::class.java) {
            configure(it)

            it.projectKey.set( jiraProjectKeyProperty )
            it.replacements.set( replacementsProperty )
        }

        workerExecutor.await()
    }
}
