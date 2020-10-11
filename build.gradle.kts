plugins {
    `maven-publish`
    signing
    java
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
}
group = "com.anatawa12.kotlinScriptRunner"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()
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

val uploadArchives: Upload by tasks

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.name
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("Kotlin Script Runner")
                packaging = "jar"
                url.set("https://github.com/anatawa12/KotlinScriptRunner/")
                description.set("Kotlin Script Runner is an Apache2 Licensed gradle plugin, for run kotlin script.")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/anatawa12/KotlinScriptRunner/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("anatawa12")
                        name.set("anatawa12")
                    }
                }
                scm {
                    url.set("https://github.com/anatawa12/KotlinScriptRunner/")
                    connection.set("scm:git@github.com:anatawa12/KotlinScriptRunner.git")
                    developerConnection.set("scm:git:git@github.com:anatawa12/KotlinScriptRunner.git")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/anatawa12/KotlinScriptRunner/issues")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(sonatypeRepositoryUrl)
            credentials {
                username = project.properties["com.anatawa12.sonatype.username"]?.toString()
                password = project.properties["com.anatawa12.sonatype.passeord"]?.toString()
            }
        }
    }
}

signing {
    isRequired = isReleaseBuild
    sign(publishing.publications["mavenJava"])
}
