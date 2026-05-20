package com.hmp.shared.resource

actual object SharedIconLoader {
    actual suspend fun loadIcon(iconName: String): ByteArray? {
        return try {
            SharedIconLoader::class.java.getResourceAsStream("/icons/$iconName.png")?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}
