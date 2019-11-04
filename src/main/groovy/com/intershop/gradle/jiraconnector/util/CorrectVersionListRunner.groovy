package com.intershop.gradle.jiraconnector.util

import com.intershop.gradle.jiraconnector.task.CorrectVersionListParameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.workers.WorkAction

import javax.inject.Inject

@CompileStatic
@Slf4j
abstract class CorrectVersionListRunner implements WorkAction<CorrectVersionListParameters> {

    @Override
    void execute() {
        JiraConnector connector = getPreparedConnector()
        connector.sortVersions(getParameters().getProjectKey().get())
        if(getParameters().getReplacements().getOrNull() != null) {
            connector.fixVersionNames(getParameters().getProjectKey().get(), getParameters().getReplacements().get())
        }
    }

    private JiraConnector getPreparedConnector() {
        if(getParameters().getBaseURL().get() &&
                getParameters().getUsername().get() &&
                getParameters().getPassword().get()) {
            JiraConnector connector = new JiraConnector(
                    getParameters().getBaseURL().get(),
                    getParameters().getUsername().get(),
                    getParameters().getPassword().get())

            if(getParameters().getSocketTimeout()) {
                connector.setSocketTimeout(getParameters().getSocketTimeout().get())
            }
            if(getParameters().getRequestTimeout()) {
                connector.setRequestTimeout(getParameters().getRequestTimeout().get())
            }

            return connector
        }
        return null
    }
}
