package io.github.luksal

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BookAdvisorApplication

fun main(args: Array<String>) {
    SpringApplication.run(BookAdvisorApplication::class.java, *args)
}