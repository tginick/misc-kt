package net.nicholasgwong.misckt

import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

const val DEFAULT_INIT_BUFFER_SIZE = 512 * 1024

class AutoBufferReallocException : RuntimeException("Failed to reallocate buffer.")

class AutoBuffer(private val bufSize: Int) : AutoCloseable {

    constructor() : this(DEFAULT_INIT_BUFFER_SIZE)

    private var buf: ByteBuffer
    private var isValid: Boolean = false
    private var nTimesAlloc = 1

    private var watermark = 0
    private var capacity = bufSize

    private var isReadMode = false

    init {
        if (bufSize <= 0) {
            throw IllegalArgumentException("Cannot allocate buffer of size $bufSize")
        }

        buf = MemoryUtil.memAlloc(bufSize)
        isValid = true
        isReadMode = false
    }

    override fun close() {
        if (!isValid) {
            throw IllegalStateException("Buffer is not Initialized")
        }
        MemoryUtil.memFree(buf)
        isValid = false
    }

    fun backingBuffer(): ByteBuffer {
        return buf
    }

    fun writeChars(chrs: String) {
        enforceInvariant(false)

        for (c in chrs) {
            writeChar(c)
        }
    }

    fun readChars(n: Int): String {
        enforceInvariant(true)
        if (n == 0) {
            return ""
        }

        if (n < 0) {
            throw IllegalArgumentException("Cannot read negative chars: $n")
        }

        val sb = StringBuilder()
        for (i in 0 until n) {
            sb.append(readChar())
        }

        return sb.toString()
    }

    fun writeInt(int: Int) {
        enforceInvariant(false)

        if (watermark + 4 > capacity) {
            growLinear()
        }

        buf.putInt(int)
    }

    fun readInt(): Int {
        enforceInvariant(true)

        return buf.int
    }

    fun writeByte(b: Byte) {
        enforceInvariant(false)

        if (watermark == capacity) {
            growLinear()
        }

        buf.put(b)
    }

    fun readByte(): Byte {
        enforceInvariant(true)

        return buf.get()
    }

    fun writeLong(l: Long) {
        enforceInvariant(false)

        if (watermark + 8 > capacity) {
            growLinear()
        }

        buf.putLong(l)
    }

    fun readLong(): Long {
        enforceInvariant(true)

        return buf.long
    }

    fun startRead() {
        if (isReadMode) {
            return
        }

        buf.flip()

        watermark = 0
        isReadMode = true
    }

    fun startWrite() {
        if (!isReadMode) {
            return
        }

        buf.clear()

        watermark = 0
        isReadMode = false
    }

    fun position(): Int {
        return buf.position()
    }

    private fun writeChar(chr: Char) {
        if (watermark == capacity) {
            growLinear()
        }

        buf.putChar(chr)

        watermark += 1
    }

    private fun readChar(): Char {
        return buf.char
    }

    private fun growLinear() {
        nTimesAlloc += 1

        val newCapacity = nTimesAlloc * bufSize
        val newBuf = MemoryUtil.memRealloc(buf, newCapacity) ?: throw AutoBufferReallocException()

        // TODO: check if we need to first free the old buffer. probably not but check anyway

        buf = newBuf
        capacity = newCapacity
    }

    private fun enforceInvariant(shouldRead: Boolean) {
        if (!isValid) {
            throw IllegalStateException("Buffer is not Initialized")
        }

        if (isReadMode != shouldRead) {
            throw IllegalStateException("Tried to perform opposite valid IO Operation. ReadMode: $isReadMode")
        }
    }
}