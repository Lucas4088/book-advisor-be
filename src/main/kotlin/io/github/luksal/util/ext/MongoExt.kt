package io.github.luksal.util.ext

import org.springframework.data.mongodb.core.query.Update


fun Update.setIfNotEmpty(field: String?, value: Any?): Update = apply {
    if (field == null) return@apply
    when (value) {
        is String -> if (value.isNotEmpty()) set(field, value)
        is Collection<*> -> if (value.isNotEmpty()) set(field, value)
        is Int -> if (value != 0) set(field, value)
        else -> if (value != null) set(field, value)
    }
}

fun Update.pushIfNotEmpty(field: String?, value: Collection<Any>?): Update = apply {
    if (field == null) return@apply
    if (!value.isNullOrEmpty())
        push(field).each(*value.toTypedArray())
}
