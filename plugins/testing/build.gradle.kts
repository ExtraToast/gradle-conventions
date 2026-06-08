plugins {
    `kotlin-dsl`
    `maven-publish`
}

base {
    archivesName.set("gradle-conventions-testing")
}

dependencies {
    implementation(project(":plugins:test-logging"))
}
