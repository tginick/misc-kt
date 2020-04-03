package net.nicholasgwong.misckt

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LRUCacheTest {
    @Test
    fun testCacheRetrieveEmpty() {
        val cache = newLRUCache<Int>(5)
        Assertions.assertEquals(0, cache.currentSize())
        Assertions.assertNull(cache.retrieve("x"))
    }

    @Test
    fun testCacheAddAndRetrieve() {
        val cache = newLRUCache<Int>(5)

        cache.add("obj1", 0)

        Assertions.assertEquals(1, cache.currentSize())

        Assertions.assertEquals(0, cache.retrieve("obj1"))
    }

    @Test
    fun testFlush() {
        val cache = newLRUCache<Int>(5)

        cache.add("obj1", 0)
        cache.flush()

        Assertions.assertEquals(0, cache.currentSize())

        Assertions.assertNull(cache.retrieve("obj1"))
    }

    @Test
    fun testLRUEviction() {
        val cache = newLRUCache<Int>(5)
        for (i in 0 until 5) {
            cache.add("obj$i", i)
        }

        Assertions.assertEquals(5, cache.currentSize())
        cache.add("newObj", 1000)

        Assertions.assertEquals(1000, cache.retrieve("newObj"))
        Assertions.assertEquals(1, cache.retrieve("obj1"))
        Assertions.assertNull(cache.retrieve("obj0"))
    }
}