/*
 *  Copyright (c) 2023 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin

plugins {
    `java-library`
    `maven-publish`
    id("signing")
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val bundlePublishingConfig = mapOf(
    "controlplane-bundle" to mapOf(
        "artifactId" to "bundle-controlplane",
        "description" to "Control Plane Package"
    ),
    "dataplane-bundle" to mapOf(
        "artifactId" to "bundle-dataplane",
        "description" to "Data Plane Package"
    ),
    "identityhub-bundle" to mapOf(
        "artifactId" to "bundle-identityhub",
        "description" to "Identity Hub Package"
    )
)

val LOCAL_REPO : String = "local"

val publishStrategies = mapOf(
    LOCAL_REPO to { repoHandler: RepositoryHandler ->
        repoHandler.mavenLocal()
    },
    "github" to { repoHandler: RepositoryHandler ->
        repoHandler.maven {
            name = "GitHubPackages"
            url = uri(System.getenv("PUBLISHING_URL"))
            credentials {
                username = System.getenv("PUBLISHING_USER")
                password = System.getenv("PUBLISHING_PASSWORD")
            }
        }
    }
)

val requestedStrategy = findProperty("publish.strategy")?.toString() ?: LOCAL_REPO

val publishRepo = if (publishStrategies.containsKey(requestedStrategy)) {
    requestedStrategy
} else {
    logger.warn("""
        |--------------------------------------------------------------------------------
        | WARNING: Publish strategy '$requestedStrategy' is not defined.
        | Defaulting to '$LOCAL_REPO' strategy.
        | Please check your 'publish.strategy' property.
        |--------------------------------------------------------------------------------
    """.trimMargin())
    LOCAL_REPO
}


subprojects {
    if (name in bundlePublishingConfig.keys) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        group = rootProject.group
        version = rootProject.version

        tasks.withType<Sign> {
            onlyIf { publishRepo != LOCAL_REPO }
        }
    }
    
    afterEvaluate {
        if (name in bundlePublishingConfig.keys) {
            val config = bundlePublishingConfig[name]!!
            val bundleGroupId = project.group.toString()
            val bundleArtifactId = config["artifactId"] as String
            val bundleDescription = config["description"] as String

            publishing {
                publications {
                    create<MavenPublication>("mavenJava") {
                        artifact(tasks.named("shadowJar").get())
                        groupId = bundleGroupId
                        artifactId = bundleArtifactId
                        version = project.version.toString()
                        
                        pom {
                            name.set(bundleArtifactId)
                            description.set(bundleDescription)
                        }
                    }
                }
                
                repositories {
                    publishStrategies[publishRepo]?.invoke(this)
                        ?: mavenLocal()
                }
            }
        }
        
        if (project.plugins.hasPlugin("com.github.johnrengelman.shadow") &&
                file("${project.projectDir}/src/main/docker/Dockerfile").exists()
        ) {

            //actually apply the plugin to the (sub-)project
            apply(plugin = "com.bmuschko.docker-remote-api")
            // configure the "dockerize" task
            val dockerTask: DockerBuildImage = tasks.create("dockerize", DockerBuildImage::class) {
                val dockerContextDir = project.projectDir
                dockerFile.set(file("$dockerContextDir/src/main/docker/Dockerfile"))
                images.add("${project.name}:latest")
                // specify platform with the -Dplatform flag:
                if (System.getProperty("platform") != null)
                    platform.set(System.getProperty("platform"))
                buildArgs.put("JAR", "build/libs/${project.name}.jar")
                inputDir.set(file(dockerContextDir))
            }
            // make sure  always runs after "dockerize" and after "copyOtel"
            dockerTask.dependsOn(tasks.named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME))
        }
    }
}

subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin("com.github.johnrengelman.shadow") &&
                file("${project.projectDir}/src/main/docker/Dockerfile").exists()
        ) {

            //actually apply the plugin to the (sub-)project
            apply(plugin = "com.bmuschko.docker-remote-api")
            // configure the "dockerize" task
            val dockerTask: DockerBuildImage = tasks.create("dockerize", DockerBuildImage::class) {
                val dockerContextDir = project.projectDir
                dockerFile.set(file("$dockerContextDir/src/main/docker/Dockerfile"))
                images.add("${project.name}:${project.version}")
                images.add("${project.name}:latest")
                // specify platform with the -Dplatform flag:
                if (System.getProperty("platform") != null)
                    platform.set(System.getProperty("platform"))
                buildArgs.put("JAR", "build/libs/${project.name}.jar")
                inputDir.set(file(dockerContextDir))
            }
            // make sure  always runs after "dockerize" and after "copyOtel"
            dockerTask.dependsOn(tasks.named(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME))
        }
    }
}