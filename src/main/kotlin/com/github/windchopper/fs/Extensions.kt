package com.github.windchopper.fs

import com.jcraft.jsch.Channel
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

fun String.trimToNull(): String? = StringUtils.trimToNull(this)

fun Int.positive(): Int? = if (this < 0) {
    null
} else {
    this
}

fun <T> ThreadLocal<T>.takeAway(): T? = get()
    ?.let {
        remove()
        it
    }

inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun <R, reified E: Exception> wrapExceptionTo(wrapper: (thrown: Exception) -> E, expression: () -> R): R = try {
    expression.invoke()
} catch (thrown: Exception) {
    when (thrown) {
        is E -> throw thrown
        else -> throw wrapper.invoke(thrown)
    }
}

fun <T, C: Channel> JSchHelper<C>.performConnected(action: (channel: C) -> T): T = wrapExceptionTo(::IOException) {
    val channel = connect()

    try {
        action(channel)
    } finally {
        disconnect()
    }
}


