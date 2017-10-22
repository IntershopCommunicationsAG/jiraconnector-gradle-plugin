package com.intershop.gradle.jiraconnector.task

import com.intershop.gradle.jiraconnector.util.JiraConnector
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@CompileStatic
abstract class JiraConnectTask extends DefaultTask {

    final PropertyState<String> baseURL = project.property(String)
    final PropertyState<String> username = project.property(String)
    final PropertyState<String> password = project.property(String)

    final PropertyState<Integer> socketTimeout = project.property(Integer)
    final PropertyState<Integer> requestTimeout = project.property(Integer)

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
        return socketTimeout.get().intValue()
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
        return requestTimeout.get().intValue()
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
