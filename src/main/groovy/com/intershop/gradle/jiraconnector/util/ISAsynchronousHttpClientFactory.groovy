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

import com.atlassian.event.api.EventPublisher
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory
import com.atlassian.httpclient.api.HttpClient
import com.atlassian.httpclient.api.factory.HttpClientOptions
import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient
import com.atlassian.sal.api.ApplicationProperties
import com.atlassian.sal.api.UrlMode
import com.atlassian.sal.api.executor.ThreadLocalContextManager
import com.atlassian.util.concurrent.NotNull
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by MRaab on 18.12.2016.
 */
@CompileStatic
class ISAsynchronousHttpClientFactory {

    @SuppressWarnings("unchecked")
    DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler, final HttpClientOptions options = new HttpClientOptions()) {

        final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new NoOpEventPublisher(),
                new RestClientApplicationProperties(serverUri),
                new ThreadLocalContextManager() {
                    @Override
                    Object getThreadLocalContext() {
                        return null
                    }

                    @Override
                    void setThreadLocalContext(Object context) {}

                    @Override
                    void clearThreadLocalContext() {}
                })

        final HttpClient httpClient = defaultHttpClientFactory.create(options)

        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient)
            }
        }
    }

    DisposableHttpClient createClient(final HttpClient client) {
        return new AtlassianHttpClientDecorator(client, null) {

            @Override
            void destroy() throws Exception {
                // This should never be implemented. This is simply creation of a wrapper
                // for AtlassianHttpClient which is extended by a destroy method.
                // Destroy method should never be called for AtlassianHttpClient coming from
                // a client! Imagine you create a RestClient, pass your own HttpClient there
                // and it gets destroy.
            }
        }
    }

    private static class NoOpEventPublisher implements EventPublisher {
        @Override
        void publish(Object o) {
        }

        @Override
        void register(Object o) {
        }

        @Override
        void unregister(Object o) {
        }

        @Override
        void unregisterAll() {
        }
    }

    /**
     * These properties are used to present JRJC as a User-Agent during http requests.
     */
    @SuppressWarnings("deprecation")
    private static class RestClientApplicationProperties implements ApplicationProperties {

        private final String baseUrl

        private RestClientApplicationProperties(URI jiraURI) {
            this.baseUrl = jiraURI.getPath()
        }

        @Override
        String getBaseUrl() {
            return baseUrl
        }

        /**
         * We'll always have an absolute URL as a client.
         */
        @NotNull
        @Override
        String getBaseUrl(UrlMode urlMode) {
            return baseUrl
        }

        @NotNull
        @Override
        String getDisplayName() {
            return "Atlassian JIRA Rest Java Client"
        }

        @NotNull
        @Override
        String getPlatformId() {
            return ApplicationProperties.PLATFORM_JIRA
        }

        @NotNull
        @Override
        String getVersion() {
            return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-com.atlassian.jira.rest.client")
        }

        @NotNull
        @Override
        Date getBuildDate() {
            // TODO implement using MavenUtils, JRJC-123
            throw new UnsupportedOperationException()
        }

        @NotNull
        @Override
        String getBuildNumber() {
            // TODO implement using MavenUtils, JRJC-123
            return String.valueOf(0)
        }

        @Override
        File getHomeDirectory() {
            return new File(".")
        }

        @Override
        String getPropertyValue(final String s) {
            throw new UnsupportedOperationException("Not implemented")
        }
    }

    private static final class MavenUtils {
        private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class)

        private static final String UNKNOWN_VERSION = "unknown"

        static String getVersion(String groupId, String artifactId) {
            final Properties props = new Properties()
            InputStream resourceAsStream = null
            try {
                resourceAsStream = MavenUtils.class.getResourceAsStream(String
                        .format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId))
                props.load(resourceAsStream)
                return props.getProperty("version", UNKNOWN_VERSION)
            } catch (Exception e) {
                logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId)
                logger.debug("Got the following exception", e)
                return UNKNOWN_VERSION
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close()
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            }
        }
    }

}
