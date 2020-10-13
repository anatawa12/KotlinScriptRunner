plugins {
    `maven-publish`
    `java-gradle-plugin`
    signing
    java
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
    id("com.jfrog.bintray") version "1.8.5"
    id("com.gradle.plugin-publish") version "0.10.1"
}

// constants for this project
group = "com.anatawa12.kotlinScriptRunner"
version = "2.0.0"
val projectWebsite = "https://github.com/anatawa12/KotlinScriptRunner/"
val projectIssueTracker = "https://github.com/anatawa12/KotlinScriptRunner/issues"
val projectDescription = "The Gradle plugin to execute some Kotlin Script as a Gradle task."
val projectVcs = "git@github.com:anatawa12/KotlinScriptRunner.git"
val projectTags = listOf("kotlin-script", "gradle-plugin", "kotlin")

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
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("KotlinScriptRunner") {
            id = "com.anatawa12.kotlinScriptRunner"
            implementationClass = "com.anatawa12.kotlinScriptRunner.KotlinRunnerPlugin"
        }
    }
}

pluginBundle {
    website = projectWebsite
    vcsUrl = projectVcs

    (plugins) {
        "KotlinScriptRunner" {
            displayName = project.name
            description = projectDescription
            tags = projectTags
        }
    }

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

//set build variables based on build type (release, continuous integration, development)
var isDevBuild: Boolean = false
var isCiBuild: Boolean = false
var isReleaseBuild: Boolean = false
if (hasProperty("release")) {
    isReleaseBuild = true
} else if (hasProperty("ci")) {
    isCiBuild = true
    version = "$version-SNAPSHOT"
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

bintray {
    user = project.findProperty("BINTRAY_USER")?.toString() ?: ""
    key = project.findProperty("BINTRAY_KEY")?.toString() ?: ""

    setPublications("mavenJava")

    with(pkg) {
        name = "$group.${project.name}"
        desc = projectDescription
        repo = "maven-snapshots"
        setLicenses("Apache-2.0")
        websiteUrl = projectWebsite
        issueTrackerUrl = projectIssueTracker
        vcsUrl = projectVcs
        publicDownloadNumbers = true
        with(version) {
            name = project.version.toString()
        }
    }
}

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
}
