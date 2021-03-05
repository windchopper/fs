package com.github.windchopper.fs.internal

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.UndeclaredThrowableException
import java.util.logging.Logger

infix fun <T> Boolean.then(parameter: T): T? = if (this) parameter else null

fun Int.positive(): Int? = if (this < 0) {
    null
} else {
    this
}

fun String.useAsDelimiterForSplit(string: String): List<String> = string.split(this)

fun <T> ThreadLocal<T>.takeAway(): T? = get()
    ?.let {
        remove()
        it
    }

inline fun <reified T> logger(): Logger = Logger.getLogger(T::class.qualifiedName)

inline fun <T, R, reified E: Exception> T.letRethrow(wrapper: (thrown: Throwable) -> E, expression: (T) -> R): R = try {
    expression.invoke(this)
} catch (thrown: Throwable) {
    throw wrap(thrown, wrapper)
}

inline fun <R, reified E: Exception> rethrow(wrapper: (thrown: Throwable) -> E, expression: () -> R): R = try {
    expression.invoke()
} catch (thrown: Throwable) {
    throw wrap(thrown, wrapper)
}

inline fun <reified E: Exception> wrap(thrown: Throwable, wrapper: (thrown: Throwable) -> E): E = when (thrown) {
    is E -> thrown
    is UndeclaredThrowableException -> wrapper.invoke(thrown.undeclaredThrowable)
    is InvocationTargetException -> wrapper.invoke(thrown.targetException)
    else -> wrapper.invoke(thrown)
}
