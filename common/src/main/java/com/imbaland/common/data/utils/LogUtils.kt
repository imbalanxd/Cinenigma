package com.imbaland.common.data.utils

import android.util.Log

fun logDebug(tag: String, content: String) {
    Log.d(tag, content)
}

fun logDebug(content: String) {
    logDebug(tag = "CinenigmaApp", content = content)
}