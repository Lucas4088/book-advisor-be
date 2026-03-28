package io.github.luksal.config

import com.github.pemistahl.lingua.api.Language
import com.github.pemistahl.lingua.api.LanguageDetector
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LinguaConfig {

    @Bean
    fun languageDetector(): LanguageDetector =
        LanguageDetectorBuilder
            .fromLanguages(Language.ENGLISH, Language.POLISH, Language.GERMAN, Language.FRENCH)
            .withMinimumRelativeDistance(0.1)
            .build()
}