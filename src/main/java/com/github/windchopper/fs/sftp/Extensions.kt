package com.github.windchopper.fs.sftp

import org.apache.commons.lang3.StringUtils

fun String.trimToNull(): String? = StringUtils.trimToNull(this)
fun Int.nullWhenNotPositive(): Int? = if (this < 0) null else this