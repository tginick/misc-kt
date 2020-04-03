package net.nicholasgwong.misckt

interface Cache<T> {
    fun add(id: String, obj: T)
    fun retrieve(id: String): T?
    fun flush()
    fun currentSize(): Int
}

