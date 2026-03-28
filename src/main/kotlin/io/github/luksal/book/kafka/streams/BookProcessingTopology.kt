package io.github.luksal.book.kafka.streams

import com.github.pemistahl.lingua.api.LanguageDetector

class BookProcessingTopology(
    private val languageDetector: LanguageDetector,
) {
    private val sourceTopic: String? = null

    private val sinkTopic: String? = null

    /*  companion object {
          val STRING_SERDE: Serde<String?>? = Serdes.String()
          val DOCUMENT_SERDE: JacksonJsonSerde<BookDocument> = JacksonJsonSerde(BookDocument::class.java)
          val ENTITY_SERDE: JacksonJsonSerde<BookEntity> = JacksonJsonSerde(BookEntity::class.java)
          val log = logger()
      }

      @Autowired
      fun buildPipeline(streamsBuilder: StreamsBuilder) =
          streamsBuilder
              .stream(sourceTopic, Consumed.with(STRING_SERDE, DOCUMENT_SERDE))
              .peek { _, value -> log.debug("*** raw value {}", value) }
              .filter { _, value -> value != null }
              .mapValues { it.mapToEntity() }
              .peek { _, value -> log.info("*** lowercase value = {}", value) }
              //todo add maybe some useful statistics calculations
              .to(sinkTopic, Produced.with(Serdes.String(), ENTITY_SERDE))*/

}