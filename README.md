# gradle-conventions

Gradle convention plugins for ExtraToast projects, published to GitHub Packages.

## Plugin IDs

| Plugin ID | Configures |
| --- | --- |
| `dev.extratoast.kotlin` | Kotlin JVM, Java toolchain defaulting to 21, strict JSR-305 handling, warnings as errors, and JUnit Platform for tests. |
| `dev.extratoast.detekt` | Detekt with defaults layered on, `allRules = false`, and a configurable config path defaulting to `config/detekt/detekt.yml`. |
| `dev.extratoast.ktlint` | Ktlint with Android mode disabled and generated/build output excluded. |
| `dev.extratoast.spring` | Kotlin conventions, Kotlin Spring, Spring Boot, Spring dependency management, shared BOMs, and standard Spring service dependencies. |
| `dev.extratoast.testing` | Java, Jacoco, test logging, an `integrationTest` source set and task, aggregate reports, and an 80% LINE coverage verification rule. |
| `dev.extratoast.test-logging` | Verbose test logging and a suite-level result summary. |
| `dev.extratoast.jooq-codegen` | A `generateJooq` task using jOOQ DDLDatabase against Flyway SQL migrations, plus generated source wiring. |

## Consumption

Each convention plugin is published as its own Maven module:

| Plugin ID | Maven coordinate |
| --- | --- |
| `dev.extratoast.kotlin` | `dev.extratoast:gradle-conventions-kotlin:<version>` |
| `dev.extratoast.detekt` | `dev.extratoast:gradle-conventions-detekt:<version>` |
| `dev.extratoast.ktlint` | `dev.extratoast:gradle-conventions-ktlint:<version>` |
| `dev.extratoast.spring` | `dev.extratoast:gradle-conventions-spring:<version>` |
| `dev.extratoast.testing` | `dev.extratoast:gradle-conventions-testing:<version>` |
| `dev.extratoast.test-logging` | `dev.extratoast:gradle-conventions-test-logging:<version>` |
| `dev.extratoast.jooq-codegen` | `dev.extratoast:gradle-conventions-jooq-codegen:<version>` |

The aggregate module `dev.extratoast:gradle-conventions:<version>` is also
published for projects that intentionally want the full set.

Add the GitHub Packages Maven repository and map each plugin id to its module in
the consuming repo's `settings.gradle.kts`:

```kotlin
pluginManagement {
    val extratoastConventionModules = mapOf(
        "dev.extratoast.kotlin" to "gradle-conventions-kotlin",
        "dev.extratoast.detekt" to "gradle-conventions-detekt",
        "dev.extratoast.ktlint" to "gradle-conventions-ktlint",
        "dev.extratoast.spring" to "gradle-conventions-spring",
        "dev.extratoast.testing" to "gradle-conventions-testing",
        "dev.extratoast.test-logging" to "gradle-conventions-test-logging",
        "dev.extratoast.jooq-codegen" to "gradle-conventions-jooq-codegen",
    )

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "ExtraToastGradleConventions"
            url = uri("https://maven.pkg.github.com/ExtraToast/gradle-conventions")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .orNull
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .orNull
            }
        }
    }
    resolutionStrategy {
        eachPlugin {
            extratoastConventionModules[requested.id.id]?.let { module ->
                useModule("dev.extratoast:$module:${requested.version}")
            }
        }
    }
}
```

Then apply the needed plugins in `build.gradle.kts` (version is the
`gradle-conventions` release, e.g. `0.2.0`):

```kotlin
plugins {
    id("dev.extratoast.kotlin") version "0.2.0"
    id("dev.extratoast.detekt") version "0.2.0"
    id("dev.extratoast.ktlint") version "0.2.0"
}
```

GitHub Packages access requires credentials with package read access. No
`*.gradle.plugin` marker artifacts are published, so the `eachPlugin` mapping
above is required.

## Configuration

Defaults can be overridden with Gradle properties:

```properties
extratoast.java.toolchain=21
extratoast.detekt.config=config/detekt/detekt.yml
extratoast.jooq.schema=public
extratoast.jooq.package=dev.extratoast.jooq.generated
extratoast.jooq.migrationLocations=filesystem:src/main/resources/db/migration
extratoast.jooq.outputDirectory=generated/jooq
```

The jOOQ plugin also exposes a `jooqCodegen` extension for per-project build
script configuration:

```kotlin
jooqCodegen {
    schemaName.set("public")
    packageName.set("com.example.generated.jooq")
    migrationLocations.set(listOf("filesystem:src/main/resources/db/migration"))
}
```
