import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.testing.Test

group = "dev.extratoast"
version = releasePleaseVersion()

val pluginProjectPaths =
    setOf(
        ":plugins:kotlin",
        ":plugins:detekt",
        ":plugins:ktlint",
        ":plugins:spring",
        ":plugins:testing",
        ":plugins:test-logging",
        ":plugins:jooq-codegen",
    )

subprojects {
    group = rootProject.group
    version = rootProject.version

    val moduleArtifactId =
        when {
            path == ":aggregate" -> "gradle-conventions"
            path in pluginProjectPaths -> "gradle-conventions-$name"
            else -> null
        }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension>("publishing") {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/ExtraToast/gradle-conventions")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }

            publications.withType(MavenPublication::class.java).configureEach {
                if (moduleArtifactId != null && (project.path == ":aggregate" || name == "pluginMaven")) {
                    artifactId = moduleArtifactId
                }
            }
        }
    }

    if (path in pluginProjectPaths) {
        tasks.withType(PublishToMavenRepository::class.java).configureEach {
            onlyIf { publication.name == "pluginMaven" }
        }
    }

    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }
}

fun Project.releasePleaseVersion(): String {
    val manifest = layout.projectDirectory.file(".release-please-manifest.json").asFile
    if (!manifest.isFile) {
        return "0.1.0"
    }

    val versions = JsonSlurper().parse(manifest) as Map<*, *>
    return versions["."]?.toString()?.takeIf(String::isNotBlank) ?: "0.1.0"
}
