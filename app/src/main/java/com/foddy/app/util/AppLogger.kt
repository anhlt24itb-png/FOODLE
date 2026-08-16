package com.foddy.app.util

import timber.log.Timber

object AppLogger {
    fun d(message: String) {
        Timber.d(message)
    }

    fun e(throwable: Throwable? = null, message: String) {
        if (throwable != null) {
            Timber.e(throwable, message)
        } else {
            Timber.e(message)
        }
    }

    fun w(message: String) {
        Timber.w(message)
    }
}
