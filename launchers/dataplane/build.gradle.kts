plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.dataplane)
    runtimeOnly(project(":extensions:dataplane-demo-extension"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks {
    val jarTask = named("jar")
    val shadow = named("shadowJar")

    named("distZip") { dependsOn(shadow) }
    named("distTar") { dependsOn(shadow) }
    named("startScripts") { dependsOn(shadow) }

    withType<CreateStartScripts> {
        dependsOn(jarTask)
    }

    val startShadowScripts = findByName("startShadowScripts")
    if (startShadowScripts != null) {
        startShadowScripts.dependsOn(jarTask)
    }
}
