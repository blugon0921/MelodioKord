plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val buildPath = File("C:/Users/blugo/Desktop")
val kotlinVersion = kotlin.coreLibrariesVersion
val mainClass = "${project.group}.${project.name.lowercase()}.MainKt" //Main File

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    implementation("dev.kord:kord-core:latest.release")
    implementation("dev.schlaubi.lavakord:kord:latest.release")
    implementation("org.slf4j:slf4j-simple:latest.release")
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
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