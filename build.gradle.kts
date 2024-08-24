import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val home = System.getProperty("user.home").replace("\\", "/")
val buildPath = File("${home}/Desktop")
val kotlinVersion = kotlin.coreLibrariesVersion
val mainClass = "${project.group}.${project.name.lowercase()}.MainKt" //Main File

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.blugon.kr/repository/maven-public/")
    maven("https://maven.topi.wtf/releases")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    implementation("dev.kord:kord-core:latest.release")
    implementation("dev.schlaubi.lavakord:kord:latest.release")
    implementation("dev.schlaubi.lavakord:lavasrc-jvm:latest.release")
//    implementation("dev.schlaubi.lavakord:lyrics-jvm:7.0.1")
    implementation("org.slf4j:slf4j-simple:latest.release")
    implementation("kr.blugon:kordmand:latest.release")
    implementation("org.json:json:20240205")
    implementation("io.github.classgraph:classgraph:latest.release")
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    jar { this.build() }
    shadowJar { this.build() }
}
fun Jar.build() {
    val file = File("./build/libs/${project.name}.jar")
    if(file.exists()) file.deleteOnExit()
    archiveBaseName.set(project.name) //Project Name
    archiveFileName.set("${project.name}.jar") //Build File Name
    archiveVersion.set(project.version.toString()) //Version
    from(sourceSets["main"].output)

    doLast {
        copy {
            from(archiveFile) //Copy from
            into(buildPath) //Copy to
        }
    }

    manifest {
        attributes["Main-Class"] = mainClass
    }
}