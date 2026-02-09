plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.luksal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    implementation("org.postgresql:postgresql:42.7.9")
    implementation("org.flywaydb:flyway-core:12.0.0")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.1.1"))
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}