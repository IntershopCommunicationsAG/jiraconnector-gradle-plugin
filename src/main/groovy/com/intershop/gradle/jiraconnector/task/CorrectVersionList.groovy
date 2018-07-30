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
import com.intershop.gradle.jiraconnector.util.JiraConnector
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerConfiguration
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
            getWorkerExecutor().submit(CorrectVersionListRunner.class, new Action<WorkerConfiguration>() {
                @Override
                void execute( WorkerConfiguration config ) {
                    config.setDisplayName( "Sort Jira issues for ${project.property('projectKey')}" )
                    config.setParams( getBaseURL(), getUsername(), getPassword(), getSocketTimeout(), getRequestTimeout(), project.property('projectKey'), getReplacements())
                    config.setIsolationMode( IsolationMode.CLASSLOADER )
                    config.classpath( project.getConfigurations().findByName(JiraConnectorExtension.JIRARESTCLIENTCONFIGURATION).getFiles() )
                }
            } )
            getWorkerExecutor().await()
        } else {
            if(! project.hasProperty('projectKey')) {
                throw new GradleException("Please specify the property 'projectKey' (JIRA project key).")
            }
        }
    }
}
