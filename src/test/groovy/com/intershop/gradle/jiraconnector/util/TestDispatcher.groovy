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

import com.squareup.okhttp.mockwebserver.Dispatcher
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.RecordedRequest

class TestDispatcher {

    static final long waitingTime = 0

    public static Dispatcher getProcessLabelTestDispatcher(Map responses, String response) {
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String line = request.getRequestLine()
                if(line.startsWith('GET /rest/api/latest/issue/UNKNOWNP-4711?expand=schema,names,transitions')) {
                    MockResponse issue_response = new MockResponse()
                            .setResponseCode(404)
                            .addHeader("Cache-Control", "no-cache")
                            .setBody('{"errorMessages":["Issue Does Not Exist"],"errors":{}}')
                    sleep(waitingTime)
                    return issue_response
                }
                if(line.startsWith('GET /rest/api/latest/issue/ISTOOLS-993?expand=schema,names,transitions')) {
                    MockResponse issue_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                            .setBody(getResponse(response))
                    sleep(waitingTime)
                    return issue_response
                }
                if(line.startsWith('GET /rest/api/latest/field')) {
                    MockResponse issue_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                            .setBody(getResponse('fieldLabels.response'))
                    sleep(waitingTime)
                    return issue_response
                }
                if(line.startsWith('PUT /rest/api/latest/issue/ISTOOLS-993')) {
                    responses.put('onebody', request.getBody().readUtf8().toString())
                    MockResponse close_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                    sleep(waitingTime)
                    return close_response
                }

                return new MockResponse()
            }
        }
        return dispatcher
    }

    public static Dispatcher getProcessVersionTestDispatcher(Map responses, String response) {
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String line = request.getRequestLine()
                if(line.startsWith('GET /rest/api/latest/issue/ISTOOLS-993?expand=schema,names,transitions')) {
                    MockResponse issue_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                            .setBody(getResponse(response))
                    sleep(waitingTime)
                    return issue_response
                }
                if(line.startsWith('GET /rest/api/latest/field')) {
                    MockResponse issue_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                            .setBody(getResponse('fieldLabels.response'))
                    println '----- start wait'
                    sleep(waitingTime)
                    println '----- wait finished'
                    return issue_response
                }
                if(line.startsWith('GET /rest/api/latest/project/ISTOOLS')) {
                    MockResponse issue_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                            .setBody(getResponse('pojectConfig.response'))
                    println '----- start wait'
                    sleep(waitingTime)
                    println '----- wait finished'
                    return issue_response
                }
                if(line.startsWith('PUT /rest/api/latest/issue/ISTOOLS-993')) {
                    responses.put('onebody', request.getBody().readUtf8().toString())
                    MockResponse close_response = new MockResponse()
                            .addHeader("Content-Type", "application/json; charset=utf-8")
                            .addHeader("Cache-Control", "no-cache")
                    println '----- start wait'
                    sleep(waitingTime)
                    println '----- wait finished'
                    return close_response
                }

                return new MockResponse()
            }
        }
        return dispatcher
    }

    private static String getResponse(String name) {
        ClassLoader classLoader = com.intershop.gradle.jiraconnector.util.TestDispatcher.class.getClassLoader();
        URL resource = classLoader.getResource(name);
        if (resource == null) {
            throw new RuntimeException("Could not find classpath resource: $name")
        }

        File resourceFile = new File(resource.toURI())
        return resourceFile.text.stripIndent()
    }
}
