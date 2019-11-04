package com.intershop.gradle.jiraconnector.task

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface CorrectVersionListParameters extends WorkParameters {

    Property<String> getBaseURL()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<Integer> getSocketTimeout()

    Property<Integer> getRequestTimeout()

    Property<String> getProjectKey()

    Property<Map<String, String>> getReplacements()
}