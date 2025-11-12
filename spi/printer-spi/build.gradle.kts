plugins {
    `java-library`
    `maven-publish`
}

val group: String by project
val version: String by project

dependencies {
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = group
            version = version
            artifactId = "printer-spi"

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
