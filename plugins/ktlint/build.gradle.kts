plugins {
    `kotlin-dsl`
    `maven-publish`
}

base {
    archivesName.set("gradle-conventions-ktlint")
}

dependencies {
    implementation("org.jlleitschuh.gradle:ktlint-gradle:14.2.0")
}
