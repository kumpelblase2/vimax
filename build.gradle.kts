import com.github.gradle.node.npm.task.NpmTask
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.github.node-gradle.node") version "3.1.1"
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    val kotlinVersion = "1.5.31"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
}

group = "de.eternalwings"
version = "1.2.1-SNAPSHOT"

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
    implementation("org.flywaydb:flyway-core:7.14.1")
    implementation("com.vladmihalcea:hibernate-types-52:2.12.1")
    implementation("com.github.h0tk3y.betterParse:better-parse-jvm:0.4.2")
    implementation("org.xerial:sqlite-jdbc:3.36.0.2")
    runtimeOnly(kotlin("scripting-jsr223"))
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	testCompileOnly("org.springframework.boot:spring-boot-starter-test")
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

tasks.withType<BootJar> {
    requiresUnpack("**/kotlin*.jar")
}
