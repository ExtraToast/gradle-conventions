import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConventionPluginSmokeTest {
    @TempDir
    Path projectDir;

    @Test
    void kotlinDetektAndKtlintPluginsRegisterTheirTasks() throws IOException {
        Files.writeString(
            projectDir.resolve("settings.gradle.kts"),
            """
            rootProject.name = "smoke"
            """.stripIndent()
        );
        Files.writeString(
            projectDir.resolve("build.gradle.kts"),
            """
            plugins {
                id("dev.extratoast.kotlin")
                id("dev.extratoast.detekt")
                id("dev.extratoast.ktlint")
            }

            repositories {
                mavenCentral()
            }
            """.stripIndent()
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

        var result =
            GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("tasks", "--all", "--stacktrace")
                .withPluginClasspath(pluginClasspath())
                .build();

        assertTrue(result.getOutput().contains("compileKotlin"), "Kotlin tasks should be registered.");
        assertTrue(result.getOutput().contains("detekt"), "Detekt tasks should be registered.");
        assertTrue(result.getOutput().contains("ktlintCheck"), "Ktlint tasks should be registered.");
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
