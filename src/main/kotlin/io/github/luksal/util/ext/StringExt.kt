package io.github.luksal.util.ext

import java.security.MessageDigest


fun String.normalize(): String =
    this.trim()
        .lowercase()
        .replace("\\s".toRegex(), "")

fun String.sha256(): String =
    MessageDigest.getInstance("SHA3-256")
        .digest(this.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
