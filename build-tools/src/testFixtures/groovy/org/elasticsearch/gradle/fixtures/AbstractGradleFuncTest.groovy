/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.gradle.fixtures

import org.elasticsearch.gradle.internal.test.InternalAwareGradleRunner
import org.elasticsearch.gradle.internal.test.NormalizeOutputGradleRunner
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.lang.management.ManagementFactory
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

import static org.elasticsearch.gradle.internal.test.TestUtils.normalizeString

abstract class AbstractGradleFuncTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File settingsFile
    File buildFile
    File propertiesFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << "rootProject.name = 'hello-world'\n"
        buildFile = testProjectDir.newFile('build.gradle')
        propertiesFile = testProjectDir.newFile('gradle.properties')
        propertiesFile <<
            "org.gradle.java.installations.fromEnv=JAVA_HOME,RUNTIME_JAVA_HOME,JAVA15_HOME,JAVA14_HOME,JAVA13_HOME,JAVA12_HOME,JAVA11_HOME,JAVA8_HOME"
    }

    File addSubProject(String subProjectPath) {
        def subProjectBuild = file(subProjectPath.replace(":", "/") + "/build.gradle")
        settingsFile << "include \"${subProjectPath}\"\n"
        subProjectBuild
    }

    GradleRunner gradleRunner(String... arguments) {
        return gradleRunner(testProjectDir.root, arguments)
    }

    GradleRunner gradleRunner(File projectDir, String... arguments) {
        return new NormalizeOutputGradleRunner(
            new InternalAwareGradleRunner(
                GradleRunner.create()
                    .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                    .withProjectDir(projectDir)
                    .withPluginClasspath()
                    .forwardOutput()
            ),
            projectDir
        ).withArguments(arguments)
    }

    def assertOutputContains(String givenOutput, String expected) {
        assert normalized(givenOutput).contains(normalized(expected))
        true
    }

    def assertOutputMissing(String givenOutput, String expected) {
        assert normalized(givenOutput).contains(normalized(expected)) == false
        true
    }

    String normalized(String input) {
        return normalizeString(input, testProjectDir.root)
    }

    File file(String path) {
        File newFile = new File(testProjectDir.root, path)
        newFile.getParentFile().mkdirs()
        newFile
    }

    File someJar(String fileName = 'some.jar') {
        File jarFolder = new File(testProjectDir.root, "jars");
        jarFolder.mkdirs()
        File jarFile = new File(jarFolder, fileName)
        JarEntry entry = new JarEntry("foo.txt");

        jarFile.withOutputStream {
            JarOutputStream target = new JarOutputStream(it)
            target.putNextEntry(entry);
            target.closeEntry();
            target.close();
        }

        return jarFile;
    }

    File internalBuild(
        File buildScript = buildFile,
        String bugfix = "7.15.2",
        String bugfixLucene = "8.9.0",
        String staged = "7.16.0",
        String stagedLucene = "8.10.0",
        String minor = "8.0.0",
        String minorLucene = "9.0.0"
    ) {
        buildScript << """plugins {
          id 'elasticsearch.global-build-info'
        }
        import org.elasticsearch.gradle.Architecture
        import org.elasticsearch.gradle.internal.info.BuildParams

        import org.elasticsearch.gradle.internal.BwcVersions.VersionPair
        import org.elasticsearch.gradle.internal.BwcVersions
        import org.elasticsearch.gradle.Version

        Version currentVersion = Version.fromString("8.1.0")
        def versionList = [
          new VersionPair(Version.fromString("$bugfix"), Version.fromString("$bugfixLucene")),
          new VersionPair(Version.fromString("$staged"), Version.fromString("$stagedLucene")),
          new VersionPair(Version.fromString("$minor"), Version.fromString("$minorLucene")),
          new VersionPair(currentVersion, Version.fromString("9.0.0"))
        ]

        BwcVersions versions = new BwcVersions(currentVersion, versionList)
        BuildParams.init { it.setBwcVersions(provider(() -> versions)) }
        """
    }

    void setupLocalGitRepo() {
        execute("git init")
        execute('git config user.email "build-tool@elastic.co"')
        execute('git config user.name "Build tool"')
        execute("git add .")
        execute('git commit -m "Initial"')
    }

    void execute(String command, File workingDir = testProjectDir.root) {
        def proc = command.execute(Collections.emptyList(), workingDir)
        proc.waitFor()
        if (proc.exitValue()) {
            System.err.println("Error running command ${command}:")
            System.err.println("Syserr: " + proc.errorStream.text)
        }
    }
}
