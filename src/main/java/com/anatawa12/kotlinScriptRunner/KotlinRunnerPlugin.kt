/**
 * Copyright 2018 anatawa12
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.anatawa12.kotlinScriptRunner

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.*
import org.gradle.process.*
import java.io.File
import javax.inject.Inject

/**
 * Created by anatawa12 on 2018/07/12.
 */
class KotlinRunnerPlugin : Plugin<Project> {
    lateinit var extension: KotlinRunnerPluginExtension
    lateinit var project: Project

    val configurations = mutableMapOf<String, Configuration>()
    val prepareKotlinHomes = mutableMapOf<String, Task>()

    internal fun getConfiguration(ktVersion: String): Configuration
            = configurations.getOrPut(ktVersion) { createConfiguration(ktVersion) }
    internal fun getPrepareKotlinHomeTask(ktVersion: String): Task
            = prepareKotlinHomes.getOrPut(ktVersion) { createPrepareKotlinHomeTask(ktVersion) }

    private fun createConfiguration(ktVersion: String): Configuration {
        project.run {
            val configuration = configurations.create("com.anatawa12.kotlinRunner.$ktVersion")
            dependencies.add(configuration.name, "org.jetbrains.kotlin:kotlin-compiler-embeddable:$ktVersion")
            dependencies.add(configuration.name, "org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$ktVersion")
            dependencies.add(configuration.name, "org.jetbrains.kotlin:kotlin-reflect:$ktVersion")
            dependencies.add(configuration.name, "org.jetbrains.kotlin:kotlin-script-runtime:$ktVersion")
            dependencies.add(configuration.name, "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$ktVersion")
            return configuration
        }
    }

    private fun createPrepareKotlinHomeTask(ktVersion: String): Task {
        project.run {
            return tasks.create("prepareKotlinHomeVersion${ktVersion}", Copy::class.java).apply {
                group = "kotlin scripting prerpare"
                from(getConfiguration(ktVersion))
                into("${extension.kotlinHome}/$ktVersion/lib")
                rename { it.replace("-$ktVersion.jar", "")+".jar" }
            }
        }
    }

    internal fun updateKotlinDependencies(oldVersion: String, newVersion: String, kotlinScriptExec: KotlinScriptExec) {
        kotlinScriptExec.run {
            if (oldVersion != "") {
                dependsOn.remove(getConfiguration(oldVersion))
                dependsOn.remove(getPrepareKotlinHomeTask(oldVersion))
            }
            dependsOn.add(getConfiguration(newVersion))
            dependsOn.add(getPrepareKotlinHomeTask(newVersion))
        }
    }

    override fun apply(project: Project) {
        this.project = project
        project.run {
            extension = KotlinRunnerPluginExtension(this)
            project.extensions.add("kotlinScript", extension)

            repositories.mavenCentral()
        }
    }
}

class KotlinRunnerPluginExtension(project: Project){
    var kotlinHome: File = project.file("${project.buildDir}/kotlin")
}

open class KotlinScriptExec @Inject constructor() : JavaExec(), JavaExecSpec {
    private val plugin = project.plugins.findPlugin(KotlinRunnerPlugin::class.java)
        ?: KotlinRunnerPlugin().apply { apply(project) }

    @Input
    var kotlinVersion: String = ""
        set(value) {
            plugin.updateKotlinDependencies(field, value, this)
            field = value
        }

    @InputFile
    var script: String? = null

    @Input
    var noJdk = false

    @Input
    var noReflect = false

    @Input
    var noStdlib = false

    @Input
    var nowarn = false

    init {
        mainClass.set("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler")
    }

    @TaskAction
    override fun exec() {
        require(kotlinVersion != "") { "kotlinVersion must be specified" }
        val args = this.args.orEmpty()
        val compilerArgs = mutableListOf(
            "-kotlin-home", "${plugin.extension.kotlinHome}/$kotlinVersion",
            "-classpath", classpath.asPath,
            "-script", script,
        )
        if (noJdk) {
            compilerArgs.add("--no-jdk-reflect")
        }
        if (noReflect) {
            compilerArgs.add("-no-reflect")
        }
        if (noStdlib) {
            compilerArgs.add("-no-stdlib")
        }
        if (nowarn) {
            compilerArgs.add("-nowarn")
        }
        compilerArgs.add("--")
        setArgs(compilerArgs)
        args(args)
        classpath = plugin.getConfiguration(kotlinVersion)
        super.exec()
    }
}
