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

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.MetadataRestClient
import com.atlassian.jira.rest.client.api.ProjectRestClient
import com.atlassian.jira.rest.client.api.RestClientException
import com.atlassian.jira.rest.client.api.VersionRestClient
import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.Project
import com.atlassian.jira.rest.client.api.domain.Version
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.VersionInput
import com.atlassian.jira.rest.client.api.domain.input.VersionPosition
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.atlassian.jira.rest.client.internal.json.VersionJsonParser
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import org.joda.time.DateTime

import java.text.ParseException

/**
 * Utility class to connect to JIRA. Makes use of
 * the atlassian server rest client library.
 */
@CompileStatic
@Slf4j
class JiraConnector {

    final String baseURL
    final String username
    final String password

    /**
     * Instantiates a RestClient based upon a JIRA url and credentials.
     *
     * @param baseUrl
     * @param username
     * @param password
     */
    JiraConnector(String baseUrl, String username, String password) {
        this.baseURL = baseUrl
        this.username = username
        this.password = password
    }

    /**
     * Create an server rest client
     * @return server rest client with credentials
     */
    public JiraRestClient getClient() {
        log.debug('Client for base url {} created.', baseURL)
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory()
        return factory.createWithBasicHttpAuthentication(baseURL.toURI(), username, password)
    }

    /**
     * Close an activ server rest client
     * @param client
     */
    public static void destroyClient(JiraRestClient client) {
        try {
            client.close()
            log.debug('Client destroyed.')
        } catch (Exception e) {
            log.warn('Closing of Jira client failed with {}', e.message)
        }
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
    public void processIssues(List<String> issueStrings, String fieldName, String stringValue, String message, boolean mergeMilestoneVersions, DateTime releaseDate) {
        // analyze selected field
        JiraField jf = getFieldMetadata(fieldName)
        // iterate over list
        issueStrings.each {String key ->
            // check for issue
            log.info('start processesing for {}', key)
            Issue issue = getIssue(key)
            if(issue) {
                //update issue
                IssueInput input = null
                if(jf.isSystem) {
                    switch (jf.system) {
                        case JiraField.FIXVERSIONS:
                            input = updateFixVersion(issue, stringValue, message, mergeMilestoneVersions, releaseDate)
                            break
                        case JiraField.VERSIONS:
                            input = updateAffectedVersions(issue, stringValue, message, mergeMilestoneVersions, releaseDate)
                            break
                        case JiraField.LABELS:
                            input = updateLabels(issue, stringValue, jf.getId())
                            break
                    }
                } else {
                    if(jf.isArray) {
                        // identify existing values
                        JSONArray jarray = (JSONArray) issue.getField(jf.getId()).getValue()

                        List<Object> values = []
                        // add new value
                        if(jf.isString) {
                            if(jarray) {
                                0.upto(jarray.length() - 1) {
                                    values.add(jarray.getString(it.intValue()))
                                }
                            }
                            values.add(stringValue)
                            log.debug('Added {} to string array {} for input', stringValue, values)
                        }
                        if(jf.isVersion) {
                            if(jarray) {
                                0.upto(jarray.length() - 1) {
                                    values.add(new VersionJsonParser().parse((JSONObject)jarray.get(it.intValue())))
                                }
                            }
                            values.add(updateVersions(issue.getProject().getKey(), stringValue, message, mergeMilestoneVersions, releaseDate))
                            log.debug('Added {} to version array {} for input', stringValue, values)
                        }
                        if(!values.isEmpty()) {
                            // create input with array
                            input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(jf.getId(), values.unique()).build()
                        }
                    } else  {
                        Object value = ''
                        if(jf.isString) {
                            value = stringValue
                            log.debug('Set {} to string field.', value)
                        }
                        if(jf.isVersion) {
                            value = updateVersions(issue.getProject().getKey(), stringValue, message, mergeMilestoneVersions, releaseDate)
                            log.debug('Set {} to version field.', value)
                        }
                        if(value) {
                            // create input with single object
                            input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldValue(jf.getId(), value).build()
                        }
                    }
                }
                if(input) {
                    JiraRestClient jrc = getClient()
                    try {
                        jrc.getIssueClient().updateIssue(issue.key, input).claim()
                        log.debug('Issue client call was successful')
                    } catch(Exception ex) {
                        log.warn('Issue ({}) was not updated. [{}]', issue.key, ex.getMessage())
                    } finally {
                        destroyClient(jrc)
                    }
                } else {
                    log.warn('Issue ({}) was not updated. [No input configuration found.]', issue.key)
                }
            }
        }
    }

    /**
     * Returns more information about the Jira field.
     *
     * @param fieldName
     * @return information about field
     */
    private JiraField getFieldMetadata(String fieldName) {
        JiraRestClient jrc = getClient()
        Field field = null
        try {
            MetadataRestClient mrClient = jrc.getMetadataClient()
            Iterable<Field> fields = mrClient.getFields().claim()
            field = (Field)fields.find { ((Field)it).getName() == fieldName }
        }catch (Exception ex) {
            log.error("It was not possible to get the metadata of '${fieldName}' from Jira", ex)
            throw new InvalidFieldnameException("It was not possible to get the metadata of '${fieldName}' from Jira")
        } finally {
            destroyClient(jrc)
        }

        if (!field) {
            throw new InvalidFieldnameException("Field '${fieldName}' is not available!")
        }

        JiraField jf = new JiraField(field)

        if (!jf.isSupported()) {
            throw new InvalidFieldnameException("Field '${field.name}' with type '${field.getSchema().getType()}' and '${field.getSchema().getItems()}' is currently not supported!")
        }

        return jf
    }

    /**
     * Return an Issue identified by its key
     * @param jiraKey a JIRA key
     * @return Issue
     */
    private Issue getIssue(String jiraKey) {
        JiraRestClient jrc = getClient()

        Issue issue = null
        try {
            issue = jrc.getIssueClient().getIssue(jiraKey).claim()
        } catch (RestClientException ex) {
            log.warn('Warning: Could not find issue key {} in server [{}].', jiraKey, ex.getMessage())
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
    private IssueInput updateFixVersion(Issue issue, String newValue, String message, boolean mergeMilestoneVersions, DateTime releaseDate) {
        // getList of versions from fixversions
        List<Version> cvl = issue.getFixVersions().toList()
        // add version
        cvl.add(updateVersions(issue.getProject().getKey(), newValue, message, mergeMilestoneVersions, releaseDate))
        // create input
        IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFixVersions(cvl.unique()).build()
        return input
    }

    /**
     * Create input for affected versions
     * @param issue     Jira Issue
     * @param versionStr  new value
     * @param message   Message will be added, if version must be created on JIRA
     * @param releaseDate  release date with time
     * @return  Input for issue
     */
    private IssueInput updateAffectedVersions(Issue issue, String versionStr, String message, boolean mergeMilestoneVersions, DateTime releaseDate) {
        // getList of versions from affected versions
        List<Version> cvl = issue.getAffectedVersions().toList()
        // add version
        cvl.add(updateVersions(issue.getProject().getKey(), versionStr, message, mergeMilestoneVersions, releaseDate))
        // create input
        IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setAffectedVersions(cvl.unique()).build()
        return input
    }

    /**
     * Create input for labels
     * @param issue     Jira Issue
     * @param valueStr  new value string
     * @param fieldId   Field input
     * @return  Input for issue
     */
    private static IssueInput updateLabels(Issue issue, String valueStr, String fieldId) {
        //get labels of this issue
        Set<String> labels = issue.getLabels()
        labels.add(valueStr)
        //create input
        IssueInput input = new IssueInputBuilder(issue.getProject(), issue.getIssueType()).setFieldInput(new FieldInput(fieldId, labels.unique())).build()
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
    private Version updateVersions(String projectKey, String versionStr, String message, boolean mergeMilestoneVersions, DateTime releaseDate) {
        JiraRestClient jcr = getClient()
        Version version = null
        int tries = 0

        while(!version && tries < 3) {
            try {
                Project jProject = jcr.getProjectClient().getProject(projectKey).claim()
                version = (Version) jProject.getVersions().find { ((Version) it).getName() == versionStr }
            } catch (Exception ex) {
                log.error('It was not possible to find the version {}. ({})', versionStr, ex.message)
            } finally {
                destroyClient(jcr)
            }
            if (!version) {
                try {
                    version = addVersion(projectKey, versionStr, message, mergeMilestoneVersions, releaseDate)
                } catch (UpdateVersionException ex) {
                    log.info('Version {} version was not created.')
                } finally {
                    destroyClient(jcr)
                }
            }

            ++tries

            if(!version && tries < 3) {
                sleep(5000)
            }
        }

        if(!version) {
            throw new UpdateVersionException("Version is null! Please check your configuration.")
        }

        log.debug('Version {} will be returned.', version.getName())
        return version
    }

    private Version addVersion(String projectKey, String versionStr, String message, boolean mergeMilestoneVersions, DateTime releaseDate){
        JiraRestClient jrc = getClient()

        Version jiraVersion = null

        try {
            VersionRestClient vClient = jrc.getVersionRestClient()
            //create version on JIRA
            log.info('Version {} will be added to the project {} with {}.', versionStr, projectKey, message)
            jiraVersion = vClient.createVersion(VersionInput.create(projectKey, versionStr, message, releaseDate, false, false)).claim()

            sortVersion(projectKey, jiraVersion)
            if(mergeMilestoneVersions) {
                mergeVersion(projectKey, jiraVersion)
            }
        }catch (Exception ex) {
            log.error("It was not possible to create a version ${versionStr}.", ex)
            throw new UpdateVersionException("It was not possible to create a version ${versionStr}. [${ex.message}].")
        } finally {
            destroyClient(jrc)
        }

        return jiraVersion
    }

    /**
     * Merge previous mile stone versions
     * @param projectKey
     * @param jiraVersion
     */
    private void mergeVersion(String projectKey, Version jiraVersion) {
        JiraRestClient jrc = getClient()
        try {
            ProjectRestClient prc = jrc.getProjectClient()
            Project jiraProject = prc.getProject(projectKey).claim()
            Map<com.intershop.release.version.Version, Version> versionMap = getSortedMap(jiraProject, jiraVersion)

            com.intershop.release.version.Version versionObject = getVersionObject(jiraVersion)

            List<Version> previousJiraVersionses = []
            versionMap.each {com.intershop.release.version.Version keyVersionObject, Version valueJiraVersion ->
                if(keyVersionObject < versionObject && keyVersionObject.normalVersion == versionObject.normalVersion) {
                    previousJiraVersionses.add(valueJiraVersion)
                }
            }
            VersionRestClient vClient = jrc.getVersionRestClient()
            previousJiraVersionses.each {Version prevVersion ->
                vClient.removeVersion(prevVersion.self, jiraVersion.self, jiraVersion.self).claim()
            }
        }catch (Exception ex) {
            log.error("It was not possible to merge previous milestone versions ${jiraVersion.name}.", ex)
            throw new UpdateVersionException("It was not possible to merge previous milestone versions ${jiraVersion.name}. [${ex.message}].")
        } finally {
            destroyClient(jrc)
        }

    }

    /**
     * Sort new version in existing list
     * @param projectKey
     * @param jiraVersion
     */
    private void sortVersion(String projectKey, Version jiraVersion) {
        JiraRestClient jrc = getClient()
        ProjectRestClient prc = jrc.getProjectClient()

        try {
            com.intershop.release.version.Version versionObject = getVersionObject(jiraVersion)

            Project jiraProject = prc.getProject(projectKey).claim()
            Map<com.intershop.release.version.Version, Version> versionMap = getSortedMap(jiraProject, jiraVersion)

            Version previousJiraVersion = null
            versionMap.each {com.intershop.release.version.Version keyVersionObject, Version valueJiraVersion ->
                if(keyVersionObject < versionObject) {
                    previousJiraVersion = valueJiraVersion
                }
            }

            VersionRestClient vClient = jrc.getVersionRestClient()
            if(! previousJiraVersion) {
                vClient.moveVersionAfter(jiraVersion.self, versionMap.values().getAt(0).self).claim()
                vClient.moveVersion(jiraVersion.self, VersionPosition.EARLIER).claim()
            } else {
                vClient.moveVersionAfter(jiraVersion.self, previousJiraVersion.self).claim()
            }

        }catch (Exception ex) {
            log.error("It was not possible to sort list for version ${jiraVersion.name}.", ex)
            throw new UpdateVersionException("It was not possible to sort list for version ${jiraVersion.name} [${ex.message}]")
        } finally {
            destroyClient(jrc)
        }
    }

    /**
     * Create Version from Jira Version
     * @param jiraVersion
     * @return
     */
    private static com.intershop.release.version.Version getVersionObject(Version jiraVersion) {
        com.intershop.release.version.Version versionObject = null
        try {
            if (jiraVersion.getName().indexOf('/') > 1) {
                versionObject = com.intershop.release.version.Version.forString(jiraVersion.getName().substring(jiraVersion.getName().indexOf('/') + 1))
            } else {
                versionObject = com.intershop.release.version.Version.forString(jiraVersion.getName())
            }
        } catch (ParseException pe) {
            throw new UpdateVersionException("It was not possible to calculate the version from Jira version ${jiraVersion.name} [${pe.message}]")
        }
        return versionObject
    }

    /**
     * Create sorted map Version - JiraVersion
     * @param jiraProject
     * @param jiraVersion
     * @return
     */
    private static Map<com.intershop.release.version.Version, Version> getSortedMap(Project jiraProject, Version jiraVersion) {
        Map<com.intershop.release.version.Version, Version> versionMap = [:]

        if(jiraVersion.getName().indexOf('/') > 1) {
            String component = jiraVersion.getName().substring(0, jiraVersion.getName().indexOf('/'))
            jiraProject.getVersions().findAll {
                it && ((Version)it).name.indexOf('/') > -1 && ((Version)it).name.indexOf('/') < ((Version)it).name.length() && component == ((Version)it).name.substring(0, ((Version)it).name.indexOf('/'))
            }.each {
                try {
                    versionMap.put(com.intershop.release.version.Version.forString(((Version)it).getName().substring(((Version)it).getName().indexOf('/') + 1)), ((Version)it))
                }catch (ParseException pe) {
                    log.warn('It was not possible to calculate the version from Jira version "{}"', ((Version)it))
                }
            }
        } else {
            jiraProject.getVersions().findAll {
                ((Version)it).name.indexOf('/') == -1
            }.each {
                try {
                    versionMap.put(com.intershop.release.version.Version.forString(((Version)it).getName()), ((Version)it))
                }catch (ParseException pe) {
                    log.warn('It was not possible to calculate the version from Jira version "{}"', ((Version)it))
                }
            }
        }

        return versionMap.sort()
    }

    public void fixVersionNames(String projectKey, Map<String,String> replacements){
        JiraRestClient jrc = getClient()
        ProjectRestClient prc = jrc.getProjectClient()
        try{
            Project p = prc.getProject(projectKey).claim()
            if(p) {
                VersionRestClient vrc = jrc.getVersionRestClient()

                Iterable<Version> version = p.getVersions()
                version.findAll{ ((Version)it).name =~ /.*\/.*/}.each {
                    String group = ((Version)it).name.substring(0, ((Version)it).name.indexOf('/'))
                    if(replacements.keySet().contains(group)) {
                        vrc.updateVersion(((Version)it).self, new VersionInput(projectKey, ((Version)it).name.replace(group, replacements.get(group)), ((Version)it).getDescription(), ((Version)it).releaseDate, ((Version)it).isArchived(), ((Version)it).isReleased())).claim()
                        println "${((Version)it).name} renamed to ${((Version)it).name.replace(group, replacements.get(group))}"
                    }
                }
            }
        } catch(Exception ex) {
            log.error('Error during sorting [{}]', ex.getMessage())
        } finally {
            destroyClient(jrc)
        }
    }

    public void sortVersions(String projectKey) {
        JiraRestClient jrc = getClient()
        ProjectRestClient prc = jrc.getProjectClient()
        try {
            Project p = prc.getProject(projectKey).claim()
            if(p) {
                Iterable<Version> version = p.getVersions()
                Map<String, Map<com.intershop.release.version.Version, Version>> vm = [:]
                version.findAll { ((Version) it).name =~ /.*\/.*/ }.each {
                    String group = ((Version)it).name.substring(0, ((Version)it).name.indexOf('/'))
                    com.intershop.release.version.Version vo = com.intershop.release.version.Version.forString(((Version)it).name.substring(((Version)it).name.indexOf('/') + 1))

                    Map<com.intershop.release.version.Version, Version> m = vm.get(group)
                    if (m) {
                        m.put(vo, ((Version)it))
                    } else {
                        m = [:]
                        m.put(vo, ((Version)it))
                    }
                    vm.put(group, m)
                }

                Map<com.intershop.release.version.Version, Version> svm = [:]
                // Create list witht component versions
                version.findAll { !(((Version) it).name =~ /.*\/.*/) }.each {
                    com.intershop.release.version.Version svo = com.intershop.release.version.Version.forString(((Version)it).name)
                    svm.put(svo, ((Version)it))
                }

                VersionRestClient vrc = jrc.getVersionRestClient()

                // sort simple versions
                svm.sort().each { com.intershop.release.version.Version vo, Version v ->
                    log.info('{} moved', v.name)
                    vrc.moveVersion(v.getSelf(), VersionPosition.LAST).claim()
                }

                // sort components versions
                vm.sort().each { String g, Map<com.intershop.release.version.Version, Version> mv ->
                    log.info('Sort simple version')
                    mv.sort().reverseEach { com.intershop.release.version.Version vo, Version v ->
                        log.info('{} moved', v.name)
                        vrc.moveVersion(v.getSelf(), VersionPosition.FIRST).claim()
                    }
                }
            }
        } catch(Exception ex) {
            log.error('Error during sorting [{}]', ex.getMessage())
        } finally {
            destroyClient(jrc)
        }
    }

}


