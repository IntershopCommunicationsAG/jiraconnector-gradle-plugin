package com.intershop.gradle.jiraconnector.util

import com.intershop.gradle.jiraconnector.task.SetIssueFieldParameters
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.joda.time.DateTime

import javax.inject.Inject
import java.util.regex.Matcher

@CompileStatic
@Slf4j
abstract class SetIssueFieldRunner implements WorkAction<SetIssueFieldParameters> {

    private String fieldValueInt

    @Override
    void execute() {
        List<String> issueList = JiraIssueParser.parse(
                getParameters().getIssueFile().get(),
                getParameters().getLinePattern().get(),
                getParameters().getJiraIssuePattern().get())

        JiraConnector connector = getPreparedConnector()
        try {
            Matcher fieldMatcher = (getParameters().getFieldValue().get() =~ /${getParameters().getFieldPattern().get()}/)
            fieldValueInt = ((List)fieldMatcher[0])[1]
        } catch(Exception ex) {
            fieldValueInt = getParameters().getFieldValue().get()
            log.warn('Fieldvalue {} is used, because field pattern does not work correctly.', fieldValueInt)
        }

        try {
            connector.processIssues(issueList, getParameters().getFieldName().get(), fieldValueInt,
                    getParameters().getVersionMessage().get(), getParameters().getMergeMilestoneVersions().get(),
                    new DateTime())
        }catch(Exception ex) {
            throw new GradleException("It was not possible to write data to Jira server with '${ex.getMessage()}'")
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
