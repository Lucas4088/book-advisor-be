pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
        kotlin("jvm") version "2.3.0"
        kotlin("plugin.spring") version "2.3.0"
        kotlin("plugin.jpa") version "2.3.0"

        id("org.springframework.boot") version "4.0.2"
        id("io.spring.dependency-management") version "1.1.7"
        id("com.expediagroup.graphql") version "8.8.1"
    }
}
rootProject.name = "book-advisor-be"