plugins {
    `java-library`
    `maven-publish`
}

val group: String by project
val version: String by project

dependencies {
    implementation(libs.edc.dataplane.spi)
    implementation(libs.edc.util.lib)
    implementation(libs.edc.dataplane.util)
    implementation("com.sun.mail:jakarta.mail:2.0.2")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = group
            version = version
            artifactId = "dataplane-demo-extension"

            from(components["java"])

            pom {
                name.set(artifactId)

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("connector-intro-dev")
                        name.set("Connector Intro Dev")
                    }
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}
