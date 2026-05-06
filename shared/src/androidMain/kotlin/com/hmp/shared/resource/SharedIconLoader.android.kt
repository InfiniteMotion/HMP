package com.hmp.shared.resource

import android.content.Context

actual object SharedIconLoader {
    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    actual suspend fun loadIcon(iconName: String): ByteArray? {
        val ctx = context ?: return null
        return try {
            ctx.assets.open("icons/$iconName.png").use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}
