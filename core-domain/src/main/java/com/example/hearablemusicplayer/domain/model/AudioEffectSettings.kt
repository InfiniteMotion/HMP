package com.example.hearablemusicplayer.domain.model

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
