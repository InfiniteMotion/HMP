package com.example.hearablemusicplayer.domain.model

/**
 * 音效预设数据类
 * @param id 预设ID
 * @param name 预设名称
 * @param description 预设描述
 */
data class AudioEffectPreset(
    val id: Int,
    val name: String,
    val description: String
)

/**
 * 音效设置数据类
 * @param equalizerPreset 当前均衡器预设ID
 * @param bassBoostLevel 低音增强级别（0-100）
 * @param isSurroundSoundEnabled 是否启用环绕声
 * @param reverbPreset 当前混响预设ID
 * @param customEqualizerLevels 自定义均衡器频段增益值
 */
data class AudioEffectSettings(
    val equalizerPreset: Int = 0,
    val bassBoostLevel: Int = 0,
    val isSurroundSoundEnabled: Boolean = false,
    val reverbPreset: Int = 0,
    val customEqualizerLevels: FloatArray = floatArrayOf()
) {
    // 重写equals和hashCode方法，因为包含数组
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioEffectSettings

        if (equalizerPreset != other.equalizerPreset) return false
        if (bassBoostLevel != other.bassBoostLevel) return false
        if (isSurroundSoundEnabled != other.isSurroundSoundEnabled) return false
        if (reverbPreset != other.reverbPreset) return false
        if (!customEqualizerLevels.contentEquals(other.customEqualizerLevels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = equalizerPreset
        result = 31 * result + bassBoostLevel
        result = 31 * result + isSurroundSoundEnabled.hashCode()
        result = 31 * result + reverbPreset
        result = 31 * result + customEqualizerLevels.contentHashCode()
        return result
    }
}

/**
 * 均衡器频段数据类
 * @param bandIndex 频段索引
 * @param frequency 频段中心频率（Hz）
 * @param level 当前增益值（dB）
 * @param minLevel 最小增益值（dB）
 * @param maxLevel 最大增益值（dB）
 */
data class EqualizerBand(
    val bandIndex: Int,
    val frequency: Int,
    val level: Float,
    val minLevel: Int,
    val maxLevel: Int
)
