package com.github.windchopper.fs.internal

import java.util.logging.Logger

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

inline fun <reified T> logger(): Logger = Logger.getLogger(T::class.qualifiedName)

inline fun <R, reified E: Exception> wrapExceptionTo(wrapper: (thrown: Exception) -> E, expression: () -> R): R = try {
    expression.invoke()
} catch (thrown: Exception) {
    when (thrown) {
        is E -> throw thrown
        else -> throw wrapper.invoke(thrown)
    }
}
