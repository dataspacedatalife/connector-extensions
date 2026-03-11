/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

rootProject.name = "connector-intro-extensions"

// this is needed to have access to snapshot builds of plugins
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven {
            url = uri(System.getenv("MAVEN_PKG_URL"))
            credentials {
                username = System.getenv("MAVEN_PKG_USERNAME")
                password = System.getenv("MAVEN_PKG_PASSWORD")
            }
        }
        mavenCentral()

    }
}

// Bundles
include(":launchers:identity-hub")
include(":launchers:controlplane")
include(":launchers:dataplane")

// SPIs
include("spi:printer-spi")

// Extensions
include("extensions:dataplane-demo-extension")
include("extensions:terminal-printer-provider-extension")
include("extensions:printer-injector-extension")
include("extensions:file-printer-provider-extension")
include("extensions:policy-demo-extension")
include("launchers")
include("launchers:file-printer")
include("launchers:terminal-printer")