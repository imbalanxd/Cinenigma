package com.imbaland.common.data.util

import android.util.Log

fun logDebug(tag: String, content: String) {
    Log.d(tag, content)
}

fun logDebug(content: String) {
    logDebug(tag = "CinenigmaApp", content = content)
}