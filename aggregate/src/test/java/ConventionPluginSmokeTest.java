import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConventionPluginSmokeTest {
    @TempDir
    Path projectDir;

    @Test
    void kotlinDetektAndKtlintPluginsRegisterTheirTasks() throws IOException {
        writeSettings("smoke");
        writeBuild(
            """
            plugins {
                id("dev.extratoast.kotlin")
                id("dev.extratoast.detekt")
                id("dev.extratoast.ktlint")
            }

            repositories {
                mavenCentral()
            }
            """
        );

        Path sourceDirectory = projectDir.resolve("src/main/kotlin/dev/extratoast/smoke");
        Files.createDirectories(sourceDirectory);
        Files.writeString(
            sourceDirectory.resolve("Smoke.kt"),
            """
            package dev.extratoast.smoke

            class Smoke
            """.stripIndent()
        );

        BuildResult result = gradle("tasks", "--all").build();

        assertTrue(result.getOutput().contains("compileKotlin"), "Kotlin tasks should be registered.");
        assertTrue(result.getOutput().contains("detekt"), "Detekt tasks should be registered.");
        assertTrue(result.getOutput().contains("ktlintCheck"), "Ktlint tasks should be registered.");
    }

    @Test
    void springPluginRegistersSpringBootTasks() throws IOException {
        writeSettings("spring-smoke");
        writeBuild(
            """
            plugins {
                id("dev.extratoast.spring")
            }

            repositories {
                mavenCentral()
            }
            """
        );

        BuildResult result = gradle("tasks", "--all").build();

        assertTrue(result.getOutput().contains("bootRun"), "Spring Boot tasks should be registered.");
        assertTrue(result.getOutput().contains("bootJar"), "Spring Boot packaging tasks should be registered.");
        assertTrue(result.getOutput().contains("compileKotlin"), "Kotlin tasks should be registered.");
    }

    @Test
    void jooqCodegenPluginGeneratesSourcesFromMigrations() throws IOException {
        writeSettings("jooq-smoke");
        writeBuild(
            """
            plugins {
                id("dev.extratoast.jooq-codegen")
            }

            repositories {
                mavenCentral()
            }
            """
        );
        Path migrations = projectDir.resolve("src/main/resources/db/migration");
        Files.createDirectories(migrations);
        Files.writeString(
            migrations.resolve("V1__create_sample_entity.sql"),
            """
            create schema public;

            create table public.sample_entity (
                id integer not null primary key,
                name varchar(255) not null
            );
            """.stripIndent()
        );

        BuildResult result = gradle("generateJooq").build();

        assertTrue(result.getOutput().contains(":generateJooq"), "jOOQ generation should run.");
        assertTrue(hasGeneratedJava(projectDir.resolve("build/generated/jooq")), "jOOQ should generate Java sources.");
    }

    @Test
    void testingPluginRunsCoverageVerificationFromCheck() throws IOException {
        writeSettings("testing-smoke");
        writeBuild(
            """
            plugins {
                id("dev.extratoast.testing")
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation(platform("org.junit:junit-bom:5.11.4"))
                testImplementation("org.junit.jupiter:junit-jupiter-api")
                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
                testRuntimeOnly("org.junit.platform:junit-platform-launcher")
            }
            """
        );
        Path sourceDirectory = projectDir.resolve("src/main/java/dev/extratoast/smoke");
        Files.createDirectories(sourceDirectory);
        Files.writeString(
            sourceDirectory.resolve("CoveredType.java"),
            """
            package dev.extratoast.smoke;

            public class CoveredType {
                public String message() {
                    return "covered";
                }
            }
            """.stripIndent()
        );
        Path testDirectory = projectDir.resolve("src/test/java/dev/extratoast/smoke");
        Files.createDirectories(testDirectory);
        Files.writeString(
            testDirectory.resolve("CoveredTypeTest.java"),
            """
            package dev.extratoast.smoke;

            import static org.junit.jupiter.api.Assertions.assertEquals;

            import org.junit.jupiter.api.Test;

            class CoveredTypeTest {
                @Test
                void returnsMessage() {
                    assertEquals("covered", new CoveredType().message());
                }
            }
            """.stripIndent()
        );

        BuildResult result = gradle("check").build();

        assertTrue(result.getOutput().contains(":integrationTest"), "Check should include integration tests.");
        assertTrue(
            result.getOutput().contains(":jacocoTestCoverageVerification"),
            "Check should include coverage verification."
        );
    }

    private void writeSettings(String rootProjectName) throws IOException {
        Files.writeString(
            projectDir.resolve("settings.gradle.kts"),
            """
            rootProject.name = "%s"
            """.formatted(rootProjectName).stripIndent()
        );
    }

    private void writeBuild(String buildScript) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle.kts"), buildScript.stripIndent());
    }

    private GradleRunner gradle(String... arguments) throws IOException {
        writeGradleProperties();
        var runnerArguments = new ArrayList<String>();
        runnerArguments.add("--stacktrace");
        runnerArguments.addAll(Arrays.asList(arguments));
        return GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(runnerArguments)
            .withPluginClasspath(pluginClasspath());
    }

    private void writeGradleProperties() throws IOException {
        String jacocoAgentJar = System.getProperty("jacocoAgentJar", "");
        String jacocoDestFile = System.getProperty("jacocoDestFile", "");
        if (jacocoAgentJar.isBlank() || jacocoDestFile.isBlank()) {
            return;
        }

        Files.writeString(
            projectDir.resolve("gradle.properties"),
            """
            org.gradle.jvmargs=-javaagent:%s=destfile=%s,append=true,dumponexit=true
            org.gradle.daemon=false
            """.formatted(jacocoAgentJar, jacocoDestFile).stripIndent()
        );
    }

    private static boolean hasGeneratedJava(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return false;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.anyMatch(path -> path.toString().endsWith(".java"));
        }
    }

    private static List<File> pluginClasspath() {
        String classpath = System.getProperty("pluginClasspath", "");
        assertFalse(classpath.isBlank(), "Plugin classpath should be configured.");
        return Arrays.stream(classpath.split(Pattern.quote(File.pathSeparator)))
            .filter(entry -> !entry.isBlank())
            .map(File::new)
            .toList();
    }
}
