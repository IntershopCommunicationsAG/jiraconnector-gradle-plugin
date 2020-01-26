/*
 * Copyright 2019 Intershop Communications AG.
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

import com.atlassian.httpclient.api.factory.HttpClientOptions
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.RestClientException
import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.Project
import com.atlassian.jira.rest.client.api.domain.Version
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.VersionInput
import com.atlassian.jira.rest.client.api.domain.input.VersionPosition
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient
import com.atlassian.jira.rest.client.internal.json.VersionJsonParser
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import org.gradle.api.GradleException
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.net.URI
import java.text.ParseException
import java.util.concurrent.TimeUnit
import com.intershop.release.version.Version as ISHVersion

open class JiraConnector(var baseURL: String,
                    var username: String,
                    var password: String,
                    var socketTimeout: Int = 3,
                    var requestTimeout: Int = 3) {

    constructor (simpleBaseURL: String,
                 simpleUsername: String,
                 simplePassword: String):
            this(simpleBaseURL,
            simpleUsername,
            simplePassword,
            3, 3)

    companion object {
        /**
         * Logger instance for logging.
         */
        val log: Logger = LoggerFactory.getLogger(this::class.java.name)

        /**
         * Close an activ server rest client
         * @param client
         */
        fun destroyClient(client: JiraRestClient) {
            try {
                client.close()
                log.debug("Client destroyed.")
            } catch (ex: Exception) {
                log.warn("Closing of Jira client failed with {}", ex.message)
            }
        }

        /**
         * Create input for labels
         * @param issue     Jira Issue
         * @param valueStr  new value string
         * @param fieldId   Field input
         * @return  Input for issue
         */
        private fun updateLabels(issue: Issue, valueStr: String, fieldId: String): IssueInput {
            //get labels of this issue
            val labels = issue.getLabels()
            labels.add(valueStr)
            //create input
            val input = IssueInputBuilder(
                    issue.getProject(), issue.getIssueType()).setFieldInput(
                    FieldInput(fieldId, labels.distinct())).build()
            return input
        }

        /**
         * Create sorted map Version - JiraVersion
         * @param jiraProject
         * @param jiraVersion
         * @return
         */
        private fun getSortedMap(jiraProject: Project, jiraVersion: Version): Map<ISHVersion, Version> {
            val versionMap = mutableMapOf<ISHVersion, Version>()

            if(jiraVersion.getName().indexOf('/') > 1) {
                val component = jiraVersion.getName().substring(0, jiraVersion.getName().indexOf('/'))
                jiraProject.versions.filter { (it.name.indexOf('/') > -1 &&
                        it.name.indexOf('/') < it.name.length &&
                        component == it.name.substring(0, it.name.indexOf('/')))
                }.forEach {
                    try {
                        versionMap.put(
                                ISHVersion.forString(
                                        it.name.substring(it.getName().indexOf('/') + 1)), it)
                    } catch(pe: ParseException) {
                        log.warn("It was not possible to calculate the version from Jira version '{}'", it)
                    }
                }
            } else {
                jiraProject.getVersions().filter { it.name.indexOf('/') == -1 }.forEach {
                    try {
                        versionMap.put(ISHVersion.forString(it.getName()), it)
                    }catch (pe: ParseException) {
                        log.warn("It was not possible to calculate the version from Jira version '{}'.", it)
                    }
                }
            }
            return versionMap.toSortedMap()
        }

        /**
         * Create Version from Jira Version
         * @param jiraVersion
         * @return
         */
        private fun getVersionObject(jiraVersion: Version): ISHVersion {
            try {
                val versionObject = if (jiraVersion.getName().indexOf('/') > 1) {
                    ISHVersion.forString(jiraVersion.getName().substring(jiraVersion.getName().indexOf('/') + 1))
                } else {
                    ISHVersion.forString(jiraVersion.getName())
                }

                return versionObject
            } catch (pe: ParseException) {
                throw UpdateVersionException("It was not possible to calculate the " +
                        "version from Jira version ${jiraVersion.name} [${pe.message}]")
            }
        }
    }

    /**
     * Adapt client options to configure the time out
     * @property returns new client options
     */
    val clientOptions : HttpClientOptions
        get() {
            val options = HttpClientOptions()
            options.setConnectionTimeout(30, TimeUnit.SECONDS)
            options.setRequestTimeout(socketTimeout, TimeUnit.MINUTES)
            options.setSocketTimeout(requestTimeout, TimeUnit.MINUTES)
            return options
        }

    /**
     * Create an server rest client.
     * @return server rest client with credentials.
     */
    fun getClient(): JiraRestClient {
        log.debug("Client for base url {} created.", baseURL)
        val factory = ISAsynchronousHttpClientFactory()
        val client = factory.createClient(URI(baseURL), BasicHttpAuthenticationHandler(username, password), clientOptions)
        return AsynchronousJiraRestClient(URI(baseURL), client)
    }

    /**
     * Process a list of issueStrings in a way, so that each one will be
     * written a string value into the given fieldname.
     * This can be also a JIRA version object.
     *
     * @param issueStrings List of Jira issue keys
     * @param fieldName    Field name for change
     * @param stringValue  String valued
     * @param releaseDate  release date with time
     * @return
     */
    fun processIssues(issueStrings: List<String>, fieldName: String, stringValue: String,
                      message: String, mergeMilestoneVersions: Boolean, releaseDate: DateTime) {

        // analyze selected field
        val jf = getFieldMetadata(fieldName)

        if(jf != null) {
            // iterate over list
            issueStrings.forEach { key: String ->
                // check for issue
                log.info("start processesing for {}", key)
                val issue = getIssue(key)
                if (issue != null) {
                    //update issue
                    var input: IssueInput? = null

                    if (jf.isSystem) {
                        input = when (jf.system) {
                            JiraField.FIXVERSIONS -> updateFixVersion(issue, stringValue, message, mergeMilestoneVersions, releaseDate)
                            JiraField.VERSIONS -> updateAffectedVersions(issue, stringValue, message, mergeMilestoneVersions, releaseDate)
                            JiraField.LABELS -> updateLabels(issue, stringValue, jf.id)
                            else -> null
                        }
                    } else {
                        if (jf.isArray) {
                            // identify existing values
                            val jarray = issue.getField(jf.id)?.value as JSONArray?
                            val values = mutableListOf<Any>()

                            // add new value
                            if (jf.isString) {
                                if (jarray != null) {
                                    for (i in 0..jarray.length() - 1) {
                                        values.add(jarray.getString(i))
                                    }
                                }
                                values.add(stringValue)
                                log.debug("Added {} to string array {} for input", stringValue, values)
                            }
                            if (jf.isVersion) {
                                if (jarray != null) {
                                    for (i in 0..jarray.length() - 1) {
                                        values.add(VersionJsonParser().parse(jarray.get(i) as JSONObject))
                                    }
                                }
                                values.add(updateVersions(issue.getProject().getKey(), stringValue, message,
                                        mergeMilestoneVersions, releaseDate))
                                log.debug("Added {} to version array {} for input", stringValue, values)
                            }
                            if (!values.isEmpty()) {
                                // create input with array
                                input = IssueInputBuilder(issue.getProject(),
                                        issue.getIssueType()).setFieldValue(jf.id, values.distinct()).build()
                            }
                        } else {

                            var value: Any = ""
                            if (jf.isString) {
                                value = stringValue
                                log.debug("Set {} to string field.", value)
                            }
                            if (jf.isVersion) {
                                value = updateVersions(issue.getProject().getKey(), stringValue,
                                        message, mergeMilestoneVersions, releaseDate)
                                log.debug("Set {} to version field.", value)
                            }
                            if (value != "") {
                                // create input with single object
                                input = IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(jf.id, value).build()
                            }
                        }
                    }
                    if (input != null) {
                        val jrc = getClient()
                        try {
                            jrc.getIssueClient().updateIssue(issue.key, input).claim()
                            log.debug("Issue client call was successful")
                        } catch (ex: Exception) {
                            log.warn("Issue ({}) was not updated. [{}]", issue.key, ex.message)
                        } finally {
                            destroyClient(jrc)
                        }
                    } else {
                        log.warn("Issue ({}) was not updated. [No input configuration found.]", issue.key)
                    }
                }
            }
        } else {
            log.warn("Issues were not updated. [metada not found.]")
        }

    }

    /**
     * Returns more information about the Jira field.
     *
     * @param fieldName
     * @return information about field
     */
    private fun getFieldMetadata(fieldName: String): JiraField? {
        val jrc = getClient()

        try {
            val mdClient = jrc.getMetadataClient()
            val field = mdClient.getFields().claim().find { it.name == fieldName }

            if(field == null) {
                throw InvalidFieldnameException("Field '${fieldName}' is not available!")
            }

            val jf = JiraField(field)

            if (! jf.isSupported) {
                throw InvalidFieldnameException("Field '${field.name}' with type '${field.schema?.getType()}' " +
                        "and '${field.schema?.getItems()}' is currently not supported!")
            }

            return jf
        }catch (ex: Exception) {
            log.error("It was not possible to get the metadata of '${fieldName}' from Jira", ex)
            throw InvalidFieldnameException("It was not possible to get the metadata of '${fieldName}' from Jira")
        } finally {
            destroyClient(jrc)
        }
    }

    /**
     * Create input for affected versions
     * @param issue     Jira Issue
     * @param versionStr  new value
     * @param message   Message will be added, if version must be created on JIRA
     * @param releaseDate  release date with time
     * @return  Input for issue
     */
    private fun updateAffectedVersions(issue: Issue, versionStr: String, message: String,
                                       mergeMilestoneVersions: Boolean, releaseDate: DateTime): IssueInput {
        val cvl: MutableList<Version> = mutableListOf()
        // getList of versions affected versions
        val vl = issue.getAffectedVersions()?.toList()

        if(vl != null) {
            cvl.addAll(vl)
        }

        // add version
        cvl.add(updateVersions(issue.getProject().getKey(), versionStr, message, mergeMilestoneVersions, releaseDate))
        // create input
        val input = IssueInputBuilder(issue.getProject(),
                issue.getIssueType()).setAffectedVersions(cvl.distinct()).build()
        return input
    }

    /**
     * Return an Issue identified by its key
     * @param jiraKey a JIRA key
     * @return Issue
     */
    private fun getIssue(jiraKey: String): Issue? {
        val jrc = getClient()
        var issue: Issue? = null

        try {
            issue = jrc.getIssueClient().getIssue(jiraKey).claim()
        } catch (ex: RestClientException) {
            log.warn("Warning: Could not find issue key {} in server [{}].", jiraKey, ex.message)
        } finally {
            destroyClient(jrc)
        }

        return issue
    }

    /**
     * Create input for fix versions
     * @param issue     Jire Issue
     * @param newValue  new value
     * @param message   Message will be added, if version must be created on JIRA
     * @param releaseDate  release date with time
     * @return  Input for issue
     */
    private fun updateFixVersion(issue: Issue, newValue: String, message: String,
                                 mergeMilestoneVersions: Boolean, releaseDate: DateTime) : IssueInput? {

        val cvl: MutableList<Version> = mutableListOf()
        // getList of versions from fixversions
        val vl = issue.getFixVersions()?.toList()

        if(vl != null) {
            cvl.addAll(vl)
        }

        // add version
        cvl.add(updateVersions(issue.getProject().getKey(), newValue, message,
                    mergeMilestoneVersions, releaseDate))
        // create input
        val input = IssueInputBuilder(issue.getProject(),
                    issue.getIssueType()).setFixVersions(cvl.distinct()).build()
        return input
    }

    /**
     * Update version list
     * @param projectKey   project key
     * @param versionStr   version string for lookup or creation
     * @param message      Message will be added, if version must be created on JIRA
     * @param releaseDate  release date with time
     * @return
     */
    private fun updateVersions(projectKey: String, versionStr: String, message: String,
                               mergeMilestoneVersions: Boolean, releaseDate: DateTime): Version {
        val jrc = getClient()
        var version: Version? = null
        var tries = 0

        while (version == null && tries < 3) {
            try {
                val jProject = jrc.getProjectClient().getProject(projectKey).claim()
                version = jProject.getVersions().find { it.name == versionStr }
            }  catch (ex: Exception) {
                log.error("It was not possible to find the version {}. ({})", versionStr, ex.message)
            }

            if (version != null) {
                try {
                    version = addVersion(projectKey, versionStr, message, mergeMilestoneVersions, releaseDate)
                } catch (ex: UpdateVersionException) {
                    log.info("Version {} version was not created. ({})", versionStr, ex.message )
                }
            }

            ++tries

            if (version == null && tries < 3) {
                sleep(5000)
            }
        }

        destroyClient(jrc)

        if(version == null) {
            throw UpdateVersionException("Version is null or empty! Please check your configuration.")
        }

        log.debug("Version {} will be returned.", version.getName())
        return version
    }

    private fun addVersion(projectKey: String, versionStr: String, message: String,
                           mergeMilestoneVersions: Boolean, releaseDate: DateTime): Version? {
        val jrc = getClient()

        try {
            val vClient = jrc.versionRestClient
            //create version on JIRA
            log.info("Version {} will be added to the project {} with {}.", versionStr, projectKey, message)
            val jiraVersion = vClient.createVersion(VersionInput.create(projectKey, versionStr,
                    message, releaseDate, false, false)).claim()

            sortVersion(projectKey, jiraVersion)
            if(mergeMilestoneVersions) {
                mergeVersion(projectKey, jiraVersion)
            }
            return jiraVersion
        }catch(ex: Exception) {
            log.error("It was not possible to create a version ${versionStr}.", ex)
            throw UpdateVersionException("It was not possible to create a version ${versionStr}. [${ex.message}].")
        } finally {
            destroyClient(jrc)
        }


    }

    /**
     * Merge previous mile stone versions
     * @param projectKey
     * @param jiraVersion
     */
    private fun mergeVersion(projectKey: String, jiraVersion: Version) {
        val jrc = getClient()
        try {
            val prc = jrc.projectClient

            val jiraProject = prc.getProject(projectKey).claim()
            val versionMap: Map<com.intershop.release.version.Version, Version> = getSortedMap(jiraProject, jiraVersion)
            val versionObject = getVersionObject(jiraVersion)
            val previousJiraVersionses = mutableListOf<Version>()

            versionMap.forEach {keyVersionObject,  valueJiraVersion ->
                if(keyVersionObject < versionObject && keyVersionObject.normalVersion == versionObject.normalVersion) {
                    previousJiraVersionses.add(valueJiraVersion)
                }
            }
            val vClient = jrc.versionRestClient
            previousJiraVersionses.forEach {prevVersion ->
                vClient.removeVersion(prevVersion.self, jiraVersion.self, jiraVersion.self).claim()
            }
        }catch (ex: Exception) {
            log.error("It was not possible to merge previous milestone versions ${jiraVersion.name}.", ex)
            throw UpdateVersionException("It was not possible to merge previous milestone versions ${jiraVersion.name}. [${ex.message}].")
        } finally {
            destroyClient(jrc)
        }

    }

    /**
     * Sort new version in existing list
     * @param projectKey
     * @param jiraVersion
     */
    private fun sortVersion(projectKey: String, jiraVersion: Version) {
        val jrc = getClient()
        val prc = jrc.projectClient

        try {
            val versionObject: ISHVersion = getVersionObject(jiraVersion)

            val jiraProject = prc.getProject(projectKey).claim()
            val versionMap = getSortedMap(jiraProject, jiraVersion)

            var previousJiraVersion: Version? = null
            versionMap.forEach {keyVersionObject: ISHVersion,  valueJiraVersion: Version ->
                if(keyVersionObject < versionObject) {
                    previousJiraVersion = valueJiraVersion
                }
            }

            val vClient = jrc.versionRestClient
            if(previousJiraVersion != null) {
                vClient.moveVersionAfter(jiraVersion.self, versionMap.values.elementAt(0).self).claim()
                vClient.moveVersion(jiraVersion.self, VersionPosition.EARLIER).claim()
            } else {
                vClient.moveVersion(jiraVersion.self, VersionPosition.LATER).claim()
            }

        }catch (ex: Exception) {
            throw UpdateVersionException("It was not possible to sort " +
                    "list for version ${jiraVersion.name} [${ex.message}]")
        } finally {
            destroyClient(jrc)
        }
    }

    fun fixVersionNames(projectKey: String, replacements: Map<String, String>){
        val jrc = getClient()
        val prc = jrc.projectClient

        try{
            val p = prc.getProject(projectKey).claim()
            if(p != null) {
                val vrc = jrc.versionRestClient
                var versionList = p.versions
                versionList.filter { it.name.matches(Regex.fromLiteral("/.*/.*/"))} .forEach {
                    val group = it.name.substring(0, it.name.indexOf('/'))
                    if(replacements.keys.contains(group)) {
                        vrc.updateVersion(it.self, VersionInput(projectKey,
                                it.name.replace(group, replacements.get(group).toString()),
                                it.getDescription(), it.releaseDate, it.isArchived(), it.isReleased())).claim()
                        println("${it.name} renamed to ${it.name.replace(group, replacements.get(group).toString())}")
                    }
                }
            }
        } catch(ex: Exception) {
            throw GradleException("Error during sorting (fix version name) [${ex.message}]")
        } finally {
            destroyClient(jrc)
        }
    }

    fun sortVersions(projectKey: String) {
        val jrc = getClient()
        val prc = jrc.projectClient
        try {
            val p = prc.getProject(projectKey).claim()
            if(p != null) {
                val version = p.getVersions()
                val vm = mutableMapOf<String, MutableMap<ISHVersion, Version>>()
                version.filter { it.name.matches(Regex.fromLiteral("/.*/.*/")) } .forEach {
                    val group = it.name.substring(0, it.name.indexOf('/'))
                    var vo: ISHVersion? = null
                    try {
                        vo = ISHVersion.forString(it.name.substring(it.name.indexOf('/') + 1))
                    } catch (ex: Exception) {
                        log.error("This is not a valid version: '{}'", it.name)
                    }
                    if(vo != null) {
                        var m: MutableMap<ISHVersion, Version>? = vm.get(group)
                        if (m != null) {
                            m.put(vo, it)
                        } else {
                            m = mutableMapOf(vo to it)
                        }
                        vm.put(group, m)
                    }
                }

                val svm = mutableMapOf<ISHVersion, Version>()
                // Create list with component versions
                version.filter { ! (it.name.matches(Regex.fromLiteral("/.*/.*/"))) } .forEach {
                    var svo: ISHVersion? = null
                    try {
                        svo = ISHVersion.forString(it.name)
                    } catch (ex: Exception) {
                        log.error("This is not a valid version: '{}'", it.name)
                    }
                    if(svo != null) {
                        svm.put(svo, it)
                    }
                }

                val vrc = jrc.versionRestClient

                // sort simple versions
                svm.toSortedMap().forEach { _ ,  v: Version ->
                    log.info("{} moved", v.name)
                    vrc.moveVersion(v.getSelf(), VersionPosition.LAST).claim()
                }

                // sort components versions
                vm.toSortedMap().forEach { _ , mv: Map<ISHVersion, Version> ->
                    log.info("Sort simple version")
                    mv.toSortedMap(Comparator.reverseOrder()).forEach { _ , v: Version ->
                        log.info("{} moved", v.name)
                        vrc.moveVersion(v.getSelf(), VersionPosition.FIRST).claim()
                    }
                }
            }
        } catch(ex: Exception) {
            throw GradleException("Error during sorting [${ex.message}]")
        } finally {
            destroyClient(jrc)
        }
    }
}