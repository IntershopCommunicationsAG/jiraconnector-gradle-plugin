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

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@CompileStatic
abstract class JiraConnectTask extends DefaultTask {

    final Property<String> baseURL = project.objects.property(String)
    final Property<String> username = project.objects.property(String)
    final Property<String> password = project.objects.property(String)

    final Property<Integer> socketTimeout = project.objects.property(Integer)
    final Property<Integer> requestTimeout = project.objects.property(Integer)

    @Input
    String getBaseURL() {
        return baseURL.get()
    }

    void setBaseURL(String baseURL) {
        this.baseURL.set(baseURL)
    }

    void setBaseURL(Provider<String> baseURL) {
        this.baseURL.set(baseURL)
    }

    @Input
    String getUsername() {
        return username.get()
    }

    void setUsername(String username) {
        this.username.set(username)
    }

    void setUsername(Provider<String> username) {
        this.username.set(username)
    }

    @Optional
    @Input
    String getPassword() {
        return password.get()
    }

    void setPassword(String password) {
        this.password.set(password)
    }

    void setPassword(Provider<String> password) {
        this.password.set(password)
    }

    @Optional
    @Input
    int getSocketTimeout() {
        return socketTimeout.getOrElse(300).intValue()
    }

    void setSocketTimeout(int socketTimeout) {
        this.socketTimeout.set(new Integer(socketTimeout))
    }

    void setSocketTimeout(Provider<Integer> socketTimeout) {
        this.socketTimeout.set(socketTimeout)
    }

    @Optional
    @Input
    int getRequestTimeout() {
        return requestTimeout.getOrElse(300).intValue()
    }

    void setRequestTimeout(int requestTimeout) {
        this.requestTimeout.set(new Integer(requestTimeout))
    }

    void setRequestTimeout(Provider<Integer> requestTimeout) {
        this.requestTimeout.set(requestTimeout)
    }

    JiraConnectTask() {
        this.group = 'Jira Tasks'
        this.outputs.upToDateWhen { false }
    }

}
