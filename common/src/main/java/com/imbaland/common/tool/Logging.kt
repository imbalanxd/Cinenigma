package com.imbaland.common.tool

import android.util.Log

fun Any.logDebug(message: String) {
    Log.d("Cinenigma Logging", "[${this.javaClass.simpleName}] $message")
}