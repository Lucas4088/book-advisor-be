package io.github.luksal.commons.jpa

interface MongoCustomRepository<T> {
    fun countApprox(): Long
}

