package io.github.luksal

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@EnableConfigurationProperties
@SpringBootApplication
class BookAdvisorApplication

fun main(args: Array<String>) {
    SpringApplication.run(BookAdvisorApplication::class.java, *args)
}