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
package com.intershop.gradle.jiraconnector.task

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import com.intershop.gradle.jiraconnector.util.CorrectVersionListRunner
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

@CompileStatic
class CorrectVersionList extends JiraConnectTask {

    @Internal
    final WorkerExecutor workerExecutor

    final Property<Map> replacements = project.objects.property(Map)

    @Optional
    @Input
    Map getReplacements() {
        return replacements.get()
    }

    void setReplacements(Map replacements) {
        this.replacements.set(replacements)
    }

    void setReplacements(Provider<Map> replacements) {
        this.replacements.set(replacements)
    }

    @Inject
    CorrectVersionList(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
        this.description = 'Correct Jira version list.'
    }

    @TaskAction
    void correctVersionList() {
        if(project.hasProperty('projectKey')) {

            WorkQueue workQueue = workerExecutor.classLoaderIsolation() {
                it.classpath.setFrom(
                        project.getConfigurations().
                                findByName(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION).getFiles()
                )
            }

            workQueue.submit(CorrectVersionListRunner.class, new Action<CorrectVersionListParameters>() {
                @Override
                void execute(CorrectVersionListParameters parameters) {
                    parameters.getBaseURL().set(getBaseURL())
                    parameters.getUsername().set(getUsername())
                    parameters.getPassword().set(getPassword())
                    parameters.getSocketTimeout().set(getSocketTimeout())
                    parameters.getRequestTimeout().set(getRequestTimeout())
                    parameters.getProjectKey().set(project.property('projectKey').toString())
                    parameters.getReplacements().set(getReplacements())
                }
            })

            getWorkerExecutor().await()
        } else {
            if(! project.hasProperty('projectKey')) {
                throw new GradleException("Please specify the property 'projectKey' (JIRA project key).")
            }
        }
    }
}
