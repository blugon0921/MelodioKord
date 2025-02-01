import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val home = System.getProperty("user.home").replace("\\", "/")
val buildPath = File("${home}/Desktop")
val mainClass = "${project.group}.${project.name.lowercase()}.MainKt" //Main File

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.blugon.kr/repository/maven-public/")
    maven("https://maven.topi.wtf/releases")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation(libs.kord)
    implementation(libs.lavakord)
    implementation(libs.lavasrcPlugin)
    implementation(libs.kordmand)
    implementation(libs.lavakordQueue)

    implementation(libs.logger)
    implementation(libs.json)
    implementation(libs.classgraph)
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