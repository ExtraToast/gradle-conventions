import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-library`
    `maven-publish`
}

base {
    archivesName.set("gradle-conventions")
}

dependencies {
    api(project(":plugins:kotlin"))
    api(project(":plugins:detekt"))
    api(project(":plugins:ktlint"))
    api(project(":plugins:spring"))
    api(project(":plugins:testing"))
    api(project(":plugins:test-logging"))
    api(project(":plugins:jooq-codegen"))

    testImplementation(gradleTestKit())
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.test {
    doFirst {
        systemProperty("pluginClasspath", sourceSets.test.get().runtimeClasspath.asPath)
    }
}
