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
package com.intershop.gradle.jiraconnector.task.jira

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

/**
 * Creates a list of Jira issue from a text file.
 */
object JiraIssueParser {

    private val logger: Logger = LoggerFactory.getLogger(JiraIssueParser::class.java)

    fun parse(path: String): List<String> {
        return this.parse(File(path), ".*", JiraConnectorExtension.JIRAISSUE_PATTERN)
    }

    /**
     * Parse the contents of a file for occurences of linePattern and
     * return each match in a unique list.
     *
     * @param file
     * @param linePattern a regex
     * @return a unique list consisting of all matches to linePattern
     */
    fun parse(file: File,
              linePattern: String = ".*",
              jiraPattern: String = JiraConnectorExtension.JIRAISSUE_PATTERN): List<String> {

        if (!file.exists())
            throw FileNotFoundException("Parsing file $file not found.")

        val result = mutableListOf<String>()

        file.forEachLine { line ->
            if(Regex(linePattern).matches(line)) {
                result.addAll(parse(line, jiraPattern))
            }
        }

        logger.info("Found following issues: {}", result.distinct())
        return result.distinct()
    }

    /**
     * Parse a single string for occurrences of jiraPattern and
     * return all as a unique list.
     *
     * @param string
     * @param jiraPattern
     * @return a unique list of occurences of linePattern in string
     */
    private fun parse(string: String, jiraPattern: String) : List<String> {
        val regex = Regex(jiraPattern)
        val matches = regex.findAll(string)

        return matches.map { it.groupValues[0] }.toList().distinct()
    }
}
