package io.github.luksal.book.kafka.streams

import com.github.pemistahl.lingua.api.LanguageDetector
import io.github.luksal.book.db.document.book.BookDocument
import io.github.luksal.book.db.jpa.model.BookEntity
import io.github.luksal.book.mapper.BookMapper.mapToEntity
import io.github.luksal.util.ext.logger
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.support.serializer.JacksonJsonSerde
import org.springframework.stereotype.Component

@Component
class BookProcessingTopology(
    private val languageDetector: LanguageDetector,
) {
    @Value($$"${app.kafka.connect.books-source-topic}")
    private val sourceTopic: String? = null

    @Value($$"${app.kafka.connect.books-sink-topic}")
    private val sinkTopic: String? = null

    companion object {
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
            .mapValues { it.mapToEntity(languageDetector.detectLanguageOf(it.title).name) }
            .peek { _, value -> log.info("*** lowercase value = {}", value) }
            //todo add maybe some useful statistics calculations
            .to(sinkTopic, Produced.with(Serdes.String(), ENTITY_SERDE))

}