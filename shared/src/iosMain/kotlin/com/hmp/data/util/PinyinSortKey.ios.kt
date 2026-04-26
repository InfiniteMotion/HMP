package com.hmp.data.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFStringTransform
import platform.CoreFoundation.CFRange
import platform.CoreFoundation.kCFStringTransformToLatin
import platform.CoreFoundation.kCFStringTransformStripDiacritics

@OptIn(ExperimentalForeignApi::class)
actual fun stringToPinyinSortKey(input: String): String {
    if (input.isEmpty()) return input
    return input
}
