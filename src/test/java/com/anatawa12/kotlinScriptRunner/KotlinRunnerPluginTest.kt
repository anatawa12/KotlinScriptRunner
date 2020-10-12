package com.anatawa12.kotlinScriptRunner

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class KotlinRunnerPluginTest {
    lateinit var tempDir: File
    lateinit var buildGradleKts: File
    lateinit var scriptPath: File

    @BeforeEach
    fun before(@TempDir tempDir: File) {
        this.tempDir = tempDir
        this.buildGradleKts = tempDir.resolve("build.gradle.kts")
        this.scriptPath = tempDir.resolve("theScript.kts")
    }

    @Test
    fun simpleExecute() {
        scriptPath.writeText("""
            println("some project")
        """.trimIndent())

        buildGradleKts.writeText("""
            plugins {
                id("com.anatawa12.kotlinScriptRunner")
            }

            val runScript by tasks.creating(com.anatawa12.kotlinScriptRunner.KotlinScriptExec::class) {
                kotlinVersion = "1.4.10"
                script = "$scriptPath"
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("runScript")
            .withPluginClasspath()
            .build()

        val task = result.task(":prepareKotlinHomeVersion1.4.10").also {
            assertNotNull(it, "prepareKotlinHomeVersion1.4.10 not found")
        }!!

        assertEquals(task.outcome, TaskOutcome.SUCCESS) { "prepareKotlinHomeVersion1.4.10 failed" }
    }

    @Test
    fun systemProperties() {
        scriptPath.writeText("""
            println("test.property: ${'$'}{System.getProperty("test.property")}")
        """.trimIndent())

        buildGradleKts.writeText("""
            plugins {
                id("com.anatawa12.kotlinScriptRunner")
            }

            val runScript by tasks.creating(com.anatawa12.kotlinScriptRunner.KotlinScriptExec::class) {
                kotlinVersion = "1.4.10"
                script = "$scriptPath"
                systemProperty("test.property", "the test property value")
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(tempDir)
            .withArguments("runScript")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("test.property: the test property value"))
    }
}
