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

import com.atlassian.event.api.EventPublisher
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory
import com.atlassian.httpclient.api.HttpClient
import com.atlassian.httpclient.api.factory.HttpClientOptions
import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient
import com.atlassian.sal.api.ApplicationProperties
import com.atlassian.sal.api.UrlMode
import com.atlassian.sal.api.executor.ThreadLocalContextManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.*

/**
 * Special HTTP client factory for JIRA rest clients.
 */
open class ISAsynchronousHttpClientFactory {

    /**
     * Creates a Jira Rest client with parameters.
     *
     * @param serverUri Jira server uri.
     * @param authenticationHandler authentication handler for rest client.
     * @param options   http client options
     *
     * @return returns a Jira rest client.
     */
    fun createClient(serverUri: URI,
                     authenticationHandler: AuthenticationHandler?,
                     options: HttpClientOptions = HttpClientOptions() ): DisposableHttpClient? {

        val defaultHttpClientFactory = DefaultHttpClientFactory(NoOpEventPublisher(),
                RestClientApplicationProperties(serverUri),
                object : ThreadLocalContextManager<Any?> {
                    override fun getThreadLocalContext(): Any? {
                        return null
                    }

                    override fun setThreadLocalContext(context: Any?) {}
                    override fun clearThreadLocalContext() {}
                })

        val httpClient: HttpClient = defaultHttpClientFactory.create(options)

        return object : AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Throws(Exception::class)
            override fun destroy() {
                defaultHttpClientFactory.dispose(httpClient)
            }
        }
    }

    /**
     * Creates a Jira Rest client with one parameter.
     *
     * @param client
     * @return returns a Jira rest client.
     */
    fun createClient(client: HttpClient?): DisposableHttpClient? {
        return object : AtlassianHttpClientDecorator(client, null) {
            @Throws(Exception::class)
            override fun destroy() {
                // This should never be implemented. This is simply creation of a wrapper
                // for AtlassianHttpClient which is extended by a destroy method.
                // Destroy method should never be called for AtlassianHttpClient coming from
                // a client! Imagine you create a RestClient, pass your own HttpClient there
                // and it gets destroy.
            }
        }
    }

    private open class NoOpEventPublisher : EventPublisher {
        override fun publish(o: Any) {}
        override fun register(o: Any) {}
        override fun unregister(o: Any) {}
        override fun unregisterAll() {}
    }

    /**
     * These properties are used to present JRJC as a User-Agent during http requests.
     */
    private class RestClientApplicationProperties constructor(jiraURI: URI) : ApplicationProperties {

        private val baseUrl: String = jiraURI.path

        override fun getBaseUrl(): String {
            return baseUrl
        }

        /**
         * We'll always have an absolute URL as a client.
         */
        override fun getBaseUrl(urlMode: UrlMode): String {
            return baseUrl
        }

        override fun getDisplayName(): String {
            return "Atlassian JIRA Rest Java Client"
        }

        override fun getPlatformId(): String {
            return ApplicationProperties.PLATFORM_JIRA
        }

        override fun getVersion(): String {
            return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-client-core")
        }

        override fun getBuildDate(): Date { // TODO implement using MavenUtils, JRJC-123
            throw UnsupportedOperationException()
        }

        override fun getBuildNumber(): String { // TODO implement using MavenUtils, JRJC-123
            return 0.toString()
        }

        override fun getHomeDirectory(): File? {
            return File(".")
        }

        override fun getPropertyValue(s: String): String {
            throw UnsupportedOperationException("Not implemented")
        }

    }

    private object MavenUtils {
        private val logger: Logger = LoggerFactory.getLogger(MavenUtils::class.java)

        /**
         * Property value for an unknown version.
         */
        const val UNKNOWN_VERSION = "unknown"

        /**
         * Get version for a special artifact over
         * Maven coordinates.
         *
         * @param groupId
         * @param artifactId
         *
         * @return artifact version
         */
        fun getVersion(groupId: String?, artifactId: String?): String {
            val props = Properties()
            var resourceAsStream: InputStream? = null
            return try {
                resourceAsStream = MavenUtils::class.java.getResourceAsStream(
                        String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId))
                props.load(resourceAsStream)
                props.getProperty("version", UNKNOWN_VERSION)
            } catch (e: Exception) {
                logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId)
                logger.debug("Got the following exception", e)
                UNKNOWN_VERSION
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close()
                    } catch (ioe: IOException) { // ignore
                    }
                }
            }
        }
    }
}
