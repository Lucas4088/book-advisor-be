package io.github.luksal.event.api

import io.github.luksal.event.service.EventService
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Controller
@RequestMapping("/api/event")
class SSEController(private val eventService: EventService) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun events(): SseEmitter =
        eventService.subscribe()

}