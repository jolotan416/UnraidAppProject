package com.jolotan.unraidapp.ui.utils

import io.github.aakira.napier.Napier

object InternalLog {
    private const val BASE_TAG = "[unraid-app]"

    fun e(tag: String, message: String) {
        Napier.e(tag = "$BASE_TAG $tag", message = message)
    }

    fun w(tag: String, message: String) {
        Napier.w(tag = "$BASE_TAG $tag", message = message)
    }

    fun d(tag: String, message: String) {
        Napier.d(tag = "$BASE_TAG $tag", message = message)
    }

    fun i(tag: String, message: String) {
        Napier.i(tag = "$BASE_TAG $tag", message = message)
    }

    fun v(tag: String, message: String) {
        Napier.v(tag = "$BASE_TAG $tag", message = message)
    }
}