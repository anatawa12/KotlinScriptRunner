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

import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.AbstractTask
import org.gradle.api.internal.artifacts.configurations.DefaultConfigurationContainer
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.collections.DefaultConfigurableFileCollection
import org.gradle.api.internal.tasks.TaskResolver
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.*
import org.gradle.process.JavaExecSpec
import org.gradle.process.JavaForkOptions
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Created by anatawa12 on 2018/07/12.
 */
class KotlinRunnerPlugin : Plugin<Project> {
	lateinit var prepareKotlinHome: Task
	lateinit var extension: KotlinRunnerPluginExtension
	override fun apply(project: Project) {
		project.run<Project, Unit> {
			extension = KotlinRunnerPluginExtension(this)
			project.extensions.add("kotlinScript", extension)
			val ktVersion = "1.2.51"
			//val extension = extensions.create("kotlinScript", KotlinRunnerPluginExtension::class.java, this)
			configurations.configure(object : Closure<Any?>(this) {
				fun doCall(it: DefaultConfigurationContainer) {
					val configuration = it.create("com.anatawa12.kotlinRunner")
					//configuration.add()
					configuration.all
				}
			})//*
			dependencies(object : Closure<Any?>(this) {
				fun doCall(it: DefaultDependencyHandler) {
					it.add("com.anatawa12.kotlinRunner", "org.jetbrains.kotlin:kotlin-compiler")
					it.add("com.anatawa12.kotlinRunner", "org.jetbrains.kotlin:kotlin-runtime")
					it.add("com.anatawa12.kotlinRunner", "org.jetbrains.kotlin:kotlin-reflect")
					it.add("com.anatawa12.kotlinRunner", "org.jetbrains.kotlin:kotlin-script-runtime")
					it.add("com.anatawa12.kotlinRunner", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")

				}
			})// */
			prepareKotlinHome = tasks.create("prepareKotlinHome", Copy::class.java).apply {
				group = "kotlin script runner"
				from(configurations.getAt("com.anatawa12.kotlinRunner"))
				into("${extension.kotlinHome}/lib")
				rename { it.replace("-$ktVersion.jar", "")+".jar" }
			}
		}
	}
}

class KotlinRunnerPluginExtension (project: Project){
	var kotlinHome: File = project.file("${project.buildDir}/kotlin")
}

open class KotlinScriptExec @Inject constructor(val fileResolver: FileResolver) : AbstractTask(), JavaExecSpec {
	private val plugin = project.plugins.findPlugin(KotlinRunnerPlugin::class.java) ?: KotlinRunnerPlugin().apply { apply(project) }
	private val javaExecHandleBuilder = this.getExecActionFactory().newJavaExecAction()

	init {
		dependsOn.add(plugin.prepareKotlinHome)
		main = "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler"
		javaExecHandleBuilder.classpath(project.configurations.getByName("com.anatawa12.kotlinRunner"))
	}

	@Inject
	protected open fun getExecActionFactory(): ExecActionFactory {
		throw UnsupportedOperationException()
	}

	var script: String? = null

	@TaskAction
	fun exec() {
		this.main = this.main
		this.jvmArgs = this.jvmArgs
		val args = this.args.toList()
		setArgs(listOf("-kotlin-home", plugin.extension.kotlinHome,
				"-classpath", _classpath.asPath,
				"-script", script ?: throw IllegalStateException("No script file specified")))
		args(args)
		this.javaExecHandleBuilder.execute()
	}

	override fun getAllJvmArgs(): List<String> {
		return this.javaExecHandleBuilder.allJvmArgs
	}

	override fun setAllJvmArgs(arguments: List<String>) {
		this.javaExecHandleBuilder.allJvmArgs = arguments
	}

	override fun setAllJvmArgs(arguments: Iterable<*>) {
		this.javaExecHandleBuilder.setAllJvmArgs(arguments)
	}

	override fun getJvmArgs(): List<String> {
		return this.javaExecHandleBuilder.jvmArgs
	}

	override fun setJvmArgs(arguments: List<String>) {
		this.javaExecHandleBuilder.jvmArgs = arguments
	}

	override fun setJvmArgs(arguments: Iterable<*>) {
		this.javaExecHandleBuilder.setJvmArgs(arguments)
	}

	override fun jvmArgs(arguments: Iterable<*>): KotlinScriptExec {
		this.javaExecHandleBuilder.jvmArgs(arguments)
		return this
	}

	override fun jvmArgs(vararg arguments: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.jvmArgs(*arguments)
		return this
	}

	override fun getSystemProperties(): Map<String, Any> {
		return this.javaExecHandleBuilder.systemProperties
	}

	override fun setSystemProperties(properties: Map<String, *>) {
		this.javaExecHandleBuilder.systemProperties = properties
	}

	override fun systemProperties(properties: Map<String, *>): KotlinScriptExec {
		this.javaExecHandleBuilder.systemProperties(properties)
		return this
	}

	override fun systemProperty(name: String?, value: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.systemProperty(name, value)
		return this
	}

	override fun getBootstrapClasspath(): FileCollection {
		return this.javaExecHandleBuilder.bootstrapClasspath
	}

	override fun setBootstrapClasspath(classpath: FileCollection) {
		this.javaExecHandleBuilder.bootstrapClasspath = classpath
	}

	override fun bootstrapClasspath(vararg classpath: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.bootstrapClasspath(*classpath)
		return this
	}

	override fun getMinHeapSize(): String? {
		return this.javaExecHandleBuilder.minHeapSize
	}

	override fun setMinHeapSize(heapSize: String?) {
		this.javaExecHandleBuilder.minHeapSize = heapSize
	}

	override fun getDefaultCharacterEncoding(): String? {
		return this.javaExecHandleBuilder.defaultCharacterEncoding
	}

	override fun setDefaultCharacterEncoding(defaultCharacterEncoding: String?) {
		this.javaExecHandleBuilder.defaultCharacterEncoding = defaultCharacterEncoding
	}

	override fun getMaxHeapSize(): String? {
		return this.javaExecHandleBuilder.maxHeapSize
	}

	override fun setMaxHeapSize(heapSize: String?) {
		this.javaExecHandleBuilder.maxHeapSize = heapSize
	}

	override fun getEnableAssertions(): Boolean {
		return this.javaExecHandleBuilder.enableAssertions
	}

	override fun setEnableAssertions(enabled: Boolean) {
		this.javaExecHandleBuilder.enableAssertions = enabled
	}

	override fun getDebug(): Boolean {
		return this.javaExecHandleBuilder.debug
	}

	@Option(option = "debug-jvm", description = "Enable debugging for the process. The process is started suspended and listening on port 5005. [INCUBATING]")
	override fun setDebug(enabled: Boolean) {
		this.javaExecHandleBuilder.debug = enabled
	}

	override fun getMain(): String? {
		return this.javaExecHandleBuilder.main
	}

	override fun setMain(mainClassName: String?): KotlinScriptExec {
		this.javaExecHandleBuilder.main = mainClassName
		return this
	}

	override fun getArgs(): List<String> {
		return this.javaExecHandleBuilder.args
	}

	override fun setArgs(applicationArgs: List<String>): KotlinScriptExec {
		this.javaExecHandleBuilder.args = applicationArgs
		return this
	}

	override fun setArgs(applicationArgs: Iterable<*>): KotlinScriptExec {
		this.javaExecHandleBuilder.setArgs(applicationArgs)
		return this
	}

	override fun args(vararg args: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.args(*args)
		return this
	}

	override fun args(args: Iterable<*>): JavaExecSpec {
		this.javaExecHandleBuilder.args(args)
		return this
	}

	private var _classpath: FileCollection = DefaultConfigurableFileCollection(fileResolver, null as TaskResolver?);

	override fun setClasspath(classpath: FileCollection): KotlinScriptExec {
		_classpath = classpath
		return this
	}

	override fun classpath(vararg paths: Any): KotlinScriptExec {
		this.classpath = this.classpath.plus(this.fileResolver.resolveFiles(*paths))
		return this
	}

	override fun getClasspath(): FileCollection {
		return _classpath
	}

	override fun copyTo(options: JavaForkOptions): KotlinScriptExec {
		this.javaExecHandleBuilder.copyTo(options)
		return this
	}

	@Optional
	@Input
	override fun getExecutable(): String? {
		return this.javaExecHandleBuilder.executable
	}

	override fun setExecutable(executable: String?) {
		this.javaExecHandleBuilder.executable = executable
	}

	override fun setExecutable(executable: Any) {
		this.javaExecHandleBuilder.setExecutable(executable)
	}

	override fun executable(executable: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.executable(executable)
		return this
	}

	@Internal
	override fun getWorkingDir(): File {
		return this.javaExecHandleBuilder.workingDir
	}

	override fun setWorkingDir(dir: File) {
		this.javaExecHandleBuilder.workingDir = dir
	}

	override fun setWorkingDir(dir: Any) {
		this.javaExecHandleBuilder.setWorkingDir(dir)
	}

	override fun workingDir(dir: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.workingDir(dir)
		return this
	}

	@Internal
	override fun getEnvironment(): Map<String, Any> {
		return this.javaExecHandleBuilder.environment
	}

	override fun setEnvironment(environmentVariables: Map<String, *>) {
		this.javaExecHandleBuilder.environment = environmentVariables
	}

	override fun environment(name: String?, value: Any): KotlinScriptExec {
		this.javaExecHandleBuilder.environment(name, value)
		return this
	}

	override fun environment(environmentVariables: Map<String, *>): KotlinScriptExec {
		this.javaExecHandleBuilder.environment(environmentVariables)
		return this
	}

	override fun copyTo(target: ProcessForkOptions): KotlinScriptExec {
		this.javaExecHandleBuilder.copyTo(target)
		return this
	}

	override fun setStandardInput(inputStream: InputStream): KotlinScriptExec {
		this.javaExecHandleBuilder.standardInput = inputStream
		return this
	}

	@Internal
	override fun getStandardInput(): InputStream {
		return this.javaExecHandleBuilder.standardInput
	}

	override fun setStandardOutput(outputStream: OutputStream): KotlinScriptExec {
		this.javaExecHandleBuilder.standardOutput = outputStream
		return this
	}

	@Internal
	override fun getStandardOutput(): OutputStream {
		return this.javaExecHandleBuilder.standardOutput
	}

	override fun setErrorOutput(outputStream: OutputStream): KotlinScriptExec {
		this.javaExecHandleBuilder.errorOutput = outputStream
		return this
	}

	@Internal
	override fun getErrorOutput(): OutputStream {
		return this.javaExecHandleBuilder.errorOutput
	}

	override fun setIgnoreExitValue(ignoreExitValue: Boolean): JavaExecSpec {
		this.javaExecHandleBuilder.isIgnoreExitValue = ignoreExitValue
		return this
	}

	@Input
	override fun isIgnoreExitValue(): Boolean {
		return this.javaExecHandleBuilder.isIgnoreExitValue
	}

	@Internal
	override fun getCommandLine(): List<String> {
		return this.javaExecHandleBuilder.commandLine
	}
}
