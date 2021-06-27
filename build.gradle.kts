import com.github.gradle.node.npm.task.NpmTask
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.21"

    id("com.github.node-gradle.node") version "3.1.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.springframework.boot") version "2.4.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.21"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "de.eternalwings"
version = "1.2-SNAPSHOT"

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }

    sourceCompatibility = "1.8"
}

tasks.compileTestKotlin {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

repositories {
	mavenCentral()
	maven("https://repo.spring.io/milestone")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("net.bramp.ffmpeg:ffmpeg:0.6.2")
    implementation("org.flywaydb:flyway-core:7.4.0")
    implementation("com.vladmihalcea:hibernate-types-52:2.4.2")
    implementation("com.github.h0tk3y.betterParse:better-parse-jvm:0.4.2")
    implementation("org.xerial:sqlite-jdbc:3.27.2.1")
    implementation("com.github.s1monw1:KtsRunner:0.0.9")
    implementation(kotlin("script-runtime"))
    runtimeOnly(kotlin("scripting-compiler-embeddable"))
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	testCompile("org.springframework.boot:spring-boot-starter-test")
}

tasks.register<NpmTask>("buildFrontend") {
    npmCommand.set(listOf("run"))
    args.set(listOf("build"))
    dependsOn(tasks.npmInstall)
}

tasks.processResources {
    from("src/frontend/dist") {
        into("public")
    }
    dependsOn("buildFrontend")
}

node {
    npmInstallCommand.set("ci")
    nodeProjectDir.set(file("${project.projectDir}/src/frontend"))
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "de.eternalwings.vima.VimaApplicationKt")
    }

    mergeServiceFiles()
    append("META-INF/spring.handlers")
    append("META-INF/spring.schemas")
    append("META-INF/spring.tooling")
    transform(PropertiesFileTransformer::class.java) {
        paths = listOf("META-INF/spring.factories")
        mergeStrategy = "append"
    }
}
