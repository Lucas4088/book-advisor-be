package io.github.luksal.config

import com.github.pemistahl.lingua.api.LanguageDetector
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LinguaConfig {

    @Bean
    fun languageDetector(): LanguageDetector =
        LanguageDetectorBuilder
            .fromAllLanguages()
            .build()
}