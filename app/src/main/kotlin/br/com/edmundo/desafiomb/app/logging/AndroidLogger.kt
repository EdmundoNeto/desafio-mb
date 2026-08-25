package br.com.edmundo.desafiomb.app.logging

import android.util.Log
import br.com.edmundo.desafiomb.app.BuildConfig
import br.com.edmundo.desafiomb.core.domain.util.Logger

class AndroidLogger : Logger {
    override fun d(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (BuildConfig.DEBUG) Log.d(tag, message, throwable)
    }

    override fun w(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (BuildConfig.DEBUG) Log.w(tag, message, throwable)
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}
