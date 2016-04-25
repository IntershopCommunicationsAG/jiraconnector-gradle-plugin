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

class JiraTestValues {

    static String LABELSNAME = 'Labels'
    static String BUILDVERSIONSNAME = 'Build Version'
    static String FIXVERSIONNAME = 'Fix Version/s'
    static String AFFECTEDVERSIONAME = 'Affects Version/s'
    static String MULTITESTVERSIONAME = 'Multitest Version'
    static String TESTEDVERSIONNAME = 'Tested Version'

    static String UNKNOWNFIELD = 'Unknown Jira Field'

    static String unknownIssue = 'UNKNOWNP-4711'
    static String issueKey = 'ISTOOLS-993'
    static String[] issueList = [issueKey, 'IS-4711']


    static String versionStr = 'platform/10.0.6'
    static String addVersionStr = 'platform/10.0.7'

    static String message = 'Version added by jiraconnector plugin test'
}
