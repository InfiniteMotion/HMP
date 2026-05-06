package com.hmp.shared.resource

expect object SharedIconLoader {
    suspend fun loadIcon(iconName: String): ByteArray?
}
