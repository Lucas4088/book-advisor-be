package io.github.luksal.config

import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class CoroutineDispatcherConfig {

    @Bean
    fun customInitializerDispatcher() =
        Executors.newFixedThreadPool(4).asCoroutineDispatcher()
}