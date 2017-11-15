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

import com.intershop.gradle.jiraconnector.util.JiraIssueParser
import com.intershop.gradle.test.util.TestDir
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JiraIssueParserSpec extends Specification{

    @TestDir
    File tempFileDir

    String simpleIssueFileContent = '''
        ISTOOLS-1234,IS-4711 | Do various steps to bar a foo
        IS-12345 | A solution to the bar-foo problem
        '''.stripIndent()

    String lineIssueFileContent = '''
        +3 | | ISTOOLS-1234,IS-4711 | Do various steps to bar a foo
        /test/ISTOOLS-1236/branch.file
        /test/ISTOOLS-1236/branch.file
        +3 | | IS-12345 | A solution to the bar-foo problem
        /test/ISTOOLS-1237/branch.file
        /test/ISTOOLS-1237/branch.file
        /test/ISTOOLS-1237/branch.file
        '''.stripIndent()

    private File writeToFile(String text) {
        File f = new File(tempFileDir, 'test.txt')
        f.parentFile.mkdirs()
        f << text
        return f
    }

    def 'Can find issues in simple file'() {
        setup:
        File testFile = writeToFile(simpleIssueFileContent)

        when:
        List<String> issueList = JiraIssueParser.parse(testFile)

        then:

        assert issueList.size() == 3
        assert issueList.contains('ISTOOLS-1234')
    }

    def 'Issue list is unique'() {
        setup:
        File testFile = writeToFile(simpleIssueFileContent)
        testFile << simpleIssueFileContent  // dup the content

        when:
        def issueList = JiraIssueParser.parse(testFile)

        then:
        assert issueList.size() == 3
    }

    def 'Can find issues in file with linepattern'() {
        setup:
        File testFile = writeToFile(lineIssueFileContent)

        when:
        List<String> issueList = JiraIssueParser.parse(testFile, '^\\+3 \\|.*')

        then:
        println issueList.size()
        issueList.size() == 3
        assert issueList.contains('ISTOOLS-1234')
    }

    def 'Result of an empty file is an empty list'() {
        setup:
        File testFile = new File(tempFileDir, 'test.txt')
        testFile.createNewFile()
        when:
        List<String> issueList = JiraIssueParser.parse(testFile)

        then:
        issueList.size() == 0
    }

}
