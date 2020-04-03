package net.nicholasgwong.misckt.ext

import java.io.PrintWriter
import java.io.StringWriter

fun formatException(e: Exception): String {
    val sw = StringWriter()
    e.printStackTrace(PrintWriter(sw))
    return sw.toString()
}