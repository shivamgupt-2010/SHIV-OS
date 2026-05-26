package com.example.core.utils

import android.util.Log

object Logger {
    private const val TAG = "ShivAI"

    fun d(message: String, tag: String = TAG) {
        Log.d(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
    }

    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }

    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }
}
