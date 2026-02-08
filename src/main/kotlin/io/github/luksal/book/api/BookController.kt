package io.github.luksal.book.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RestController
import java.awt.print.Book

@Controller
@RestController(value = "/books")
class BookController {

    fun search() : List<Book> {

    }
}