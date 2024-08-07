plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val buildPath = File("C:/Users/blugo/Desktop")
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
    implementation("dev.schlaubi.lavakord:lavasrc-jvm:7.0.1")
//    implementation("dev.schlaubi.lavakord:lyrics-jvm:7.0.1")
    implementation("org.slf4j:slf4j-simple:latest.release")
//    implementation("kr.blugon:kordmand:latest.release")
    implementation("kr.blugon:kordmand:0.0.3")
    implementation("org.json:json:20240205")
    implementation("io.github.classgraph:classgraph:latest.release")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_21.toString()
    }

    jar {
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

    shadowJar {
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
}