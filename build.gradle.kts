import com.jfrog.bintray.gradle.BintrayExtension
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Date

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
 * limitations under the License.
 */
plugins {
    // build performance
    id("com.gradle.build-scan") version "3.0"

    // project plugins
    `java-gradle-plugin`
    groovy

    // test coverage
    jacoco

    // ide plugin
    idea

    // publish plugin
    `maven-publish`

    // intershop version plugin
    id("com.intershop.gradle.scmversion") version "6.0.0"

    // plugin for documentation
    id("org.asciidoctor.jvm.convert") version "2.3.0"

    // plugin for publishing to Gradle Portal
    id("com.gradle.plugin-publish") version "0.10.1"

    // plugin for publishing to jcenter
    id("com.jfrog.bintray") version "1.8.4"
}

scm {
    version.initialVersion = "1.0.0"
}

buildScan {
    termsOfServiceUrl   = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

// release configuration
group = "com.intershop.gradle.jiraconnector"
description = "Gradle Plugin for editing Atlassian Jira Issues"
version = scm.version.version

repositories {
    jcenter()
}

val pluginId = "com.intershop.gradle.jiraconnector"

gradlePlugin {
    plugins {
        create("jiraconnectorPlugin") {
            id = pluginId
            implementationClass = "com.intershop.gradle.jiraconnector.JiraConnectorPlugin"
            displayName = project.name
            description = project.description
        }
    }
}

pluginBundle {
    website = "https://github.com/IntershopCommunicationsAG/${project.name}"
    vcsUrl = "https://github.com/IntershopCommunicationsAG/${project.name}"
    tags = listOf("intershop", "gradle", "plugin", "atlassian", "jira")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot'"
}

configurations {
    val integrationTestCompile by configurations.creating {
        extendsFrom(configurations["implementation"])
    }

    val integrationTestRuntime by configurations.creating {
        extendsFrom(configurations["integrationTestCompile"])
    }
}

sourceSets.create("integrationTest") {
    withConvention(GroovySourceSet::class) {
        groovy.setSrcDirs(mutableListOf("src/unittest/groovy"))
    }
    resources.srcDir("src/unittest/resources")

    compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
}

idea {
    module {
        scopes["COMPILE"]!!["plus"]!!.add(configurations.getByName("integrationTestCompile"))
        scopes["TEST"]!!["plus"]!!.add(configurations.getByName("integrationTestRuntime"))
    }
}

tasks {
    withType<Test>().configureEach {
        systemProperty("intershop.gradle.versions", "5.6.3")

        if (!System.getenv("JIRAUSER").isNullOrBlank() &&
                !System.getenv("JIRAPASSWD").isNullOrBlank() &&
                !System.getenv("JIRAURL").isNullOrBlank()) {
            systemProperty("jira_url_config", System.getenv("JIRAURL"))
            systemProperty("jira_user_config", System.getenv("JIRAUSER"))
            systemProperty("jira_passwd_config", System.getenv("JIRAPASSWD"))
        }

        if (!System.getProperty("GITUSER").isNullOrBlank() &&
                !System.getProperty("GITPASSWD").isNullOrBlank() &&
                !System.getProperty("GITURL").isNullOrBlank()) {
            systemProperty("jira_url_config", System.getProperty("JIRAURL"))
            systemProperty("jira_user_config", System.getProperty("JIRAUSER"))
            systemProperty("jira_passwd_config", System.getProperty("JIRAPASSWD"))
        }


        dependsOn("jar")
    }

    val integrationTest = register<Test>("integrationTest") {
        description = "Runs the integration tests"
        group = "verification"
        testClassesDirs = sourceSets.getByName("integrationTest").output.classesDirs
        classpath = sourceSets.getByName("integrationTest").runtimeClasspath
        mustRunAfter("test")
    }

    getByName("check")?.dependsOn(integrationTest)

    val copyAsciiDoc = register<Copy>("copyAsciiDoc") {
        includeEmptyDirs = false

        val outputDir = file("$buildDir/tmp/asciidoctorSrc")
        val inputFiles = fileTree(mapOf("dir" to rootDir,
                "include" to listOf("**/*.asciidoc"),
                "exclude" to listOf("build/**")))

        inputs.files.plus( inputFiles )
        outputs.dir( outputDir )

        doFirst {
            outputDir.mkdir()
        }

        from(inputFiles)
        into(outputDir)
    }

    withType<AsciidoctorTask> {
        dependsOn("copyAsciiDoc")

        setSourceDir(file("$buildDir/tmp/asciidoctorSrc"))
        sources(delegateClosureOf<PatternSet> {
            include("README.asciidoc")
        })

        outputOptions {
            setBackends(listOf("html5", "docbook"))
        }

        options = mapOf( "doctype" to "article",
                "ruby"    to "erubis")
        attributes = mapOf(
                "latestRevision"        to  project.version,
                "toc"                   to "left",
                "toclevels"             to "2",
                "source-highlighter"    to "coderay",
                "icons"                 to "font",
                "setanchors"            to "true",
                "idprefix"              to "asciidoc",
                "idseparator"           to "-",
                "docinfo1"              to "true")
    }

    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true

            html.destination = File(project.buildDir, "jacocoHtml")
        }

        val jacocoTestReport by tasks
        jacocoTestReport.dependsOn("test")
    }

    getByName("bintrayUpload")?.dependsOn("asciidoctor")
    getByName("jar")?.dependsOn("asciidoctor")

    register<Jar>("sourceJar") {
        description = "Creates a JAR that contains the source code."

        from(sourceSets.getByName("main").allSource)
        archiveClassifier.set("sources")
    }

    register<Jar>("javaDoc") {
        dependsOn(groovydoc)
        from(groovydoc)
        getArchiveClassifier().set("javadoc")
    }
}

publishing {
    publications {
        create("intershopMvn", MavenPublication::class.java) {

            from(components["java"])
            artifact(tasks.getByName("sourceJar"))
            artifact(tasks.getByName("javaDoc"))

            artifact(File(buildDir, "docs/asciidoc/html5/README.html")) {
                classifier = "reference"
            }

            artifact(File(buildDir, "docs/asciidoc/docbook/README.xml")) {
                classifier = "docbook"
            }

            pom.withXml {
                val root = asNode()
                root.appendNode("name", project.name)
                root.appendNode("description", project.description)
                root.appendNode("url", "https://github.com/IntershopCommunicationsAG/${project.name}")

                val scm = root.appendNode("scm")
                scm.appendNode("url", "https://github.com/IntershopCommunicationsAG/${project.name}")
                scm.appendNode("connection", "git@github.com:IntershopCommunicationsAG/${project.name}.git")

                val org = root.appendNode("organization")
                org.appendNode("name", "Intershop Communications")
                org.appendNode("url", "http://intershop.com")

                val license = root.appendNode("licenses").appendNode("license")
                license.appendNode("name", "Apache License, Version 2.0")
                license.appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0")
                license.appendNode("distribution", "repo")
            }
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")

    setPublications("intershopMvn")

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = project.name
        userOrg = "intershopcommunicationsag"

        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/IntershopCommunicationsAG/${project.name}"

        desc = project.description
        websiteUrl = "https://github.com/IntershopCommunicationsAG/${project.name}"
        issueTrackerUrl = "https://github.com/IntershopCommunicationsAG/${project.name}/issues"

        setLabels("intershop", "gradle", "plugin", "atlassian", "jira")
        publicDownloadNumbers = true

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version.toString()
            desc = "${project.description} ${project.version}"
            released  = Date().toString()
            vcsTag = project.version.toString()
        })
    })
}

dependencies {
    implementation("com.google.guava:guava:20.0")
    implementation("com.intershop.gradle.version:extended-version:3.0.1")

    // see also configuration in JiraConnectorPlugin
    implementation("com.atlassian.jira:jira-rest-java-client-core:5.1.6")
    implementation("com.atlassian.jira:jira-rest-java-client-api:5.1.6")
    implementation("io.atlassian.fugue:fugue:4.7.2")

    testImplementation("com.intershop.gradle.test:test-gradle-plugin:3.1.0-dev.2")
    testImplementation(gradleTestKit())

    // mock webserver
    testImplementation("com.squareup.okhttp3:okhttp:3.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.11.0")
}
