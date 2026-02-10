package io.github.luksal.book.mail

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(val mailSender: JavaMailSender) {

    fun sendEmail(to: String, subject: String, text: String) {
        val message = mailSender.createMimeMessage()
        val helper = org.springframework.mail.javamail.MimeMessageHelper(message, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text, true)
        mailSender.send(message)
    }
}