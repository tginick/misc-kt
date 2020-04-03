package net.nicholasgwong.misckt.ext

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun slf4jLogger(clazz: KClass<*>): Logger {
    return LoggerFactory.getLogger(clazz.java)
}