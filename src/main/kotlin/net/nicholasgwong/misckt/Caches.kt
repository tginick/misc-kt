package net.nicholasgwong.misckt

import net.nicholasgwong.misckt.impl.LRUCache

fun <T> newLRUCache(cacheSize: Int): Cache<T> {
    return LRUCache(cacheSize)
}