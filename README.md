# misc-kt
Miscellaneous Kotlin Utilities

These are some Kotlin-based utility classes I wrote way back when I was working on a side game project. Maybe someone will find these useful.

## What's in here?

* LRUCache - A generic Least Recently Used cache implementation.
* AutoBuffer - An wrapper around ByteBuffer that automatically grows as data is written.
    * Currently leverages the jemalloc wrapper provided by LWJGL but any allocator that can provide a ByteBuffer can be used.
    * Implements AutoCloseable so short-lived buffers can be cleaned up elegantly.
    * Must switch between read and write mode explicitly. This mitigates misuse of the ByteBuffer API (ever forget to call ByteBuffer.flip?). 

## License
