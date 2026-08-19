package com.hearablemusic.player.ui.common.util

import com.hmp.domain.enum.LabelName

/**
 * LabelName扩展属性,获取对应的图标文件名(用于SharedIconLoader加载)
 */
val LabelName.iconName: String
    get() = this.name.lowercase()
