plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("com.expediagroup.graphql")
}

group = "io.github.luksal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.spring.boot.starters)

    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.postgres)

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.reactor)

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.spring.cloud.get()}"))
    implementation(libs.spring.cloud.openfeign)

    implementation(libs.graphql.kotlin.client)
    implementation(libs.resilience4j.spring.boot)

    implementation(libs.jsoup)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}
