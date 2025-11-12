plugins {
    `java-library`
    `maven-publish`
}

val group: String by project
val version: String by project

dependencies {
    implementation(libs.edc.spi.boot)
    implementation(project(":spi:printer-spi"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = group
            version = version
            artifactId = "printer-injector-extension"

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
