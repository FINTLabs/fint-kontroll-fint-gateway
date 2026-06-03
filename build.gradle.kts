plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "no.novari"
version = "1.0-SNAPSHOT"
description = " "

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.fintlabs.no/releases")
    }
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.retry:spring-retry")
//    implementation("org.flywaydb:flyway-core")
//    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
   implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation ("no.novari:kafka:5.0.0")
    implementation ("no.novari:flyt-cache:2.0.1")
    implementation ("no.fintlabs:fint-kontroll-auth:1.3.8")
    implementation ("no.fintlabs:fint-resource-server-security:1.1.0")
    implementation ("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

//    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}