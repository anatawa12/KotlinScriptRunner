plugins {
    id("maven")
    id("signing")
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
    id("groovy")
}
group = "com.anatawa12.kotlinScriptRunner"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // use stdlib built in gradle
    //implementation(kotlin("stdlib-jdk8"))

    implementation(gradleApi())
    implementation(localGroovy())
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

// for deploy
val sourceJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.javadoc.get())
    from(tasks.javadoc.get().destinationDir)
    archiveClassifier.set("javadoc")
}

artifacts {
    archives(tasks.jar.get())
    archives(sourceJar)
    archives(javadocJar)
}

//set build variables based on build type (release, continuous integration, development)
var isDevBuild: Boolean = false
var isCiBuild: Boolean = false
var isReleaseBuild: Boolean = false
var sonatypeRepositoryUrl: String = ""
if (hasProperty("release")) {
    isReleaseBuild = true
    sonatypeRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
} else if (hasProperty("ci")) {
    isCiBuild = true
    version = "$version-SNAPSHOT"
    sonatypeRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
} else {
    isDevBuild = true
    version = "$version-SNAPSHOT"
}

if (isReleaseBuild) println("release build")
if (isCiBuild) println("ci build")
if (isDevBuild) println("dev build")

repositories {
    mavenCentral()
}

signing {
    isRequired = isReleaseBuild
    sign(configurations.archives.get())
}

val uploadArchives: Upload by tasks

//*
uploadArchives.apply {
    repositories {
        if (isDevBuild) {
            mavenLocal()
        } else {

            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    withGroovyBuilder {
                        "repository"("url" to sonatypeRepositoryUrl) {
                            "authentication"(
                                "userName" to project.properties["com.anatawa12.sonatype.username"],
                                "password" to project.properties["com.anatawa12.sonatype.passeord"]
                            )
                        }
                    }

                    pom.project {

                        withGroovyBuilder {
                            "name" ("Kotlin Script Runner")
                            "packaging" ("jar")
                            "url" ("https://github.com/anatawa12/KotlinScriptRunner/")
                            "description" ("Kotlin Script Runner is an Apache2 Licensed gradle plugin, for run kotlin script.")
                            "licenses" {
                                "license" {
                                    "name"("Apache License 2.0")
                                    "url"("https://github.com/anatawa12/KotlinScriptRunner/blob/master/LICENSE")
                                    "distribution"("repo")
                                }
                            }
                            "developers" {
                                "developer" {
                                    "name" ("anatawa12")
                                }
                            }
                            "scm" {
                                "url" ("https://github.com/anatawa12/KotlinScriptRunner/")
                                "connection" ("scm:git@github.com:anatawa12/KotlinScriptRunner.git")
                                "developerConnection" ("scm:git:git@github.com:anatawa12/KotlinScriptRunner.git")
                            }
                            "organization" {
                                "name" ("com.github.anatawa12")
                                "url" ("https://github.com/anatawa12")
                            }
                            "issueManagement" {
                                "system" ("GitHub")
                                "url" ("https://github.com/anatawa12/KotlinScriptRunner/issues")
                            }
                        }
                    }
                }
            }
        }
    }
}
