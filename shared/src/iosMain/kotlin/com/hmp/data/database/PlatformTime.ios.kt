package com.hmp.data.database

import platform.Foundation.NSDate

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
