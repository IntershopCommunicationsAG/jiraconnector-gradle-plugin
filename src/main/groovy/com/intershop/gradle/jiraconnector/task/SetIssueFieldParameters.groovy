package com.intershop.gradle.jiraconnector.task

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface SetIssueFieldParameters extends WorkParameters {

    Property<String> getBaseURL()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<File> getIssueFile()

    Property<String> getLinePattern()

    Property<String> getFieldPattern()

    Property<String> getJiraIssuePattern()

    Property<String> getFieldValue()

    Property<String> getFieldName()

    Property<String> getVersionMessage()

    Property<Boolean> getMergeMilestoneVersions()

    Property<Integer> getSocketTimeout()

    Property<Integer> getRequestTimeout()
}