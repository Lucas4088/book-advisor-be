package io.github.luksal.book.config

import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class CoroutineDispatcherConfig {

    @Bean
    fun ioInitializerDispatcher() =
        Executors.newFixedThreadPool(4).asCoroutineDispatcher()
}