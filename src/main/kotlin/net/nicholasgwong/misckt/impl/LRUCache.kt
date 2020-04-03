package net.nicholasgwong.misckt.impl

import net.nicholasgwong.misckt.Cache
import net.nicholasgwong.misckt.ext.slf4jLogger
import org.slf4j.LoggerFactory
import java.lang.ref.SoftReference

internal class LRUQueue {
    internal class LRUQueueEntry(val id: String) {
        var prev: LRUQueueEntry? = null
        var next: LRUQueueEntry? = null
    }

    internal class CacheException(msg: String): Exception(msg)

    private val begin: LRUQueueEntry = LRUQueueEntry("%BEGIN%")
    private val end: LRUQueueEntry = LRUQueueEntry("%END")
    private var currentLast: LRUQueueEntry? = null

    init {
        begin.next = end
        end.next = null

        end.prev = begin
        begin.prev = null
    }

    fun enqueue(id: String): LRUQueueEntry {
        val newEntry = LRUQueueEntry(id)

        newEntry.next = begin.next
        newEntry.prev = begin

        newEntry.next!!.prev = newEntry

        begin.next = newEntry

        if (currentLast == null) {
            currentLast = newEntry
        }

        return newEntry
    }

    fun use(queueEntry: LRUQueueEntry) {
        val eNext = queueEntry.next
        val ePrev = queueEntry.prev

        val second = begin.next

        if (eNext != null && ePrev != null && second != null) {
            ePrev.next = eNext
            eNext.prev = ePrev

            second.prev = queueEntry
            begin.next = queueEntry

            queueEntry.prev = begin
            queueEntry.next = second

            currentLast = end.prev
        } else {
            throw CacheException("Unexpected cache state USING a cache entry: " + queueEntry.id)
        }
    }

    fun discard(queueEntry: LRUQueueEntry) {
        val eNext = queueEntry.next
        val ePrev = queueEntry.prev

        if (eNext != null && ePrev != null) {
            ePrev.next = eNext
            eNext.prev = ePrev

            currentLast = end.prev
        } else {
            throw CacheException("Unexpected cache state DISCARDING a cache entry: " + queueEntry.id)
        }
    }

    fun getLast(): String? {
        return currentLast?.id
    }

    fun evictLast() {
        val secondToLast = currentLast?.prev

        if (secondToLast != null) {
            secondToLast.next = end
            end.prev = secondToLast

            currentLast = secondToLast
        }
    }

    fun flush() {
        // this should cause all the interior elements to be GC'ed
        begin.next = end
        end.prev = begin

        currentLast = null
    }

}

internal class LRUCacheEntry<T>(obj: T, val lruQueueEntry: LRUQueue.LRUQueueEntry) {
    val softObj: SoftReference<T> = SoftReference(obj)
}

internal class LRUCache<T>(val maxCacheSize: Int) : Cache<T> {
    companion object {
        private val LOG = slf4jLogger(LRUCache::class)
    }

    private val lruQueue = LRUQueue()
    private val cachedData: MutableMap<String, LRUCacheEntry<T>> = HashMap()
    private var currentCacheSize = 0

    override fun add(id: String, obj: T) {
        if (cachedData.containsKey(id)) {
            return
        }

        val newCacheEntry = LRUCacheEntry(obj, lruQueue.enqueue(id))
        cachedData[id] = newCacheEntry

        currentCacheSize += 1

        if (currentCacheSize > maxCacheSize) {
            evictLRU()
            currentCacheSize -= 1
        }
    }

    override fun retrieve(id: String): T? {
        if (!cachedData.containsKey(id)) {
            return null
        }

        val entry = cachedData[id]
        if (entry!!.softObj.get() == null) {
            // this entry has expired
            try {
                lruQueue.discard(entry.lruQueueEntry)
            } catch (e: LRUQueue.CacheException) {
                LOG.error("Error discarding id $id: ${e.message}. Flushing cache")
                flush()
            }

            // regardless return null
            return null
        }

        val entryData: T? = entry.softObj.get()
        try {
            lruQueue.use(entry.lruQueueEntry)
        } catch (e: LRUQueue.CacheException) {
            LOG.error("Error retrieving id $id: ${e.message}. Flushing cache")
            flush()

            return null
        }


        return entryData
    }

    override fun flush() {
        lruQueue.flush()
        cachedData.clear()

        currentCacheSize = 0
    }

    private fun evictLRU() {
        val lastKey = lruQueue.getLast()

        if (lastKey != null) {
            val entry = cachedData[lastKey]

            if (entry != null) {
                cachedData.remove(lastKey)
                lruQueue.evictLast()
            }
        }
    }

    override fun currentSize(): Int {
        return currentCacheSize
    }
}