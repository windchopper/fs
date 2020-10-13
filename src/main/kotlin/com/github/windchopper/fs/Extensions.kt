package com.github.windchopper.fs

import org.apache.commons.lang3.StringUtils

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

inline fun <R, reified E: Exception> wrapExceptionTo(wrapper: (thrown: Exception) -> E, expression: () -> R): R = try {
    expression.invoke()
} catch (thrown: Exception) {
    when (thrown) {
        is E -> throw thrown
        else -> throw wrapper.invoke(thrown)
    }
}

