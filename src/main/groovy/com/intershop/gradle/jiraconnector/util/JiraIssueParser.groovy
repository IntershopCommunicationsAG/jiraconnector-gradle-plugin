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

package com.intershop.gradle.jiraconnector.util

import com.intershop.gradle.jiraconnector.extension.JiraConnectorExtension
import groovy.util.logging.Slf4j

/**
 * Utility class to parse content for occurrence of a pattern
 * and returning a list of found matches as a string list.
 */
@Slf4j
class JiraIssueParser {

    /**
     * Parse the contents of a file for occurences of linePattern and
     * return each match in a unique list.
     *
     * @param file
     * @param linePattern a regex
     * @return a unique list consisting of all matches to linePattern
     */
    static List<String> parse(File file, String linePattern = '', String jiraPattern = JiraConnectorExtension.JIRAISSUE_PATTERN) {
        if (!file.exists())
            throw new FileNotFoundException("Parsing file ${file} not found.")
        def result = new ArrayList<String>()

        String intLinePattern = linePattern ?: '.*'

        file.eachLine { line ->
            if(line =~ /${intLinePattern}/) {
                result.addAll(line.findAll(jiraPattern))
            }
        }

        log.info('Found following issues: {}', result.unique())
        return result.unique()
    }

    /**
     * Parse a single string for occurrences of jiraPattern and
     * return all as a unique list.
     *
     * @param string
     * @param jiraPattern
     * @return a unique list of occurences of linePattern in string
     */
    static List<String> parse(String string, String jiraPattern) {
        return string.findAll(jiraPattern).unique()
    }
}
