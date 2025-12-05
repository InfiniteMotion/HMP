/**
 * 音效设置数据类
 * @param equalizerPreset 当前均衡器预设ID
 * @param bassBoostLevel 低音增强级别（0-100）
 * @param isSurroundSoundEnabled 是否启用环绕声
 * @param reverbPreset 当前混响预设ID
 * @param customEqualizerLevels 自定义均衡器频段增益值
 */
export class AudioEffectSettings {
  equalizerPreset: number = 0;
  bassBoostLevel: number = 0;
  isSurroundSoundEnabled: boolean = false;
  reverbPreset: number = 0;
  customEqualizerLevels: number[] = [];

  constructor(
    equalizerPreset: number = 0,
    bassBoostLevel: number = 0,
    isSurroundSoundEnabled: boolean = false,
    reverbPreset: number = 0,
    customEqualizerLevels: number[] = []
  ) {
    this.equalizerPreset = equalizerPreset;
    this.bassBoostLevel = bassBoostLevel;
    this.isSurroundSoundEnabled = isSurroundSoundEnabled;
    this.reverbPreset = reverbPreset;
    this.customEqualizerLevels = customEqualizerLevels;
  }

  // 比较两个数组是否相等
  private arraysEqual(arr1: number[], arr2: number[]): boolean {
    if (arr1.length !== arr2.length) {
      return false;
    }
    for (let i = 0; i < arr1.length; i++) {
      if (arr1[i] !== arr2[i]) {
        return false;
      }
    }
    return true;
  }

  // 重写equals方法，用于比较对象是否相等
  equals(other: AudioEffectSettings): boolean {
    if (this === other) {
      return true;
    }

    return (
      this.equalizerPreset === other.equalizerPreset &&
      this.bassBoostLevel === other.bassBoostLevel &&
      this.isSurroundSoundEnabled === other.isSurroundSoundEnabled &&
      this.reverbPreset === other.reverbPreset &&
      this.arraysEqual(this.customEqualizerLevels, other.customEqualizerLevels)
    );
  }

  // 生成hashCode值
  hashCode(): number {
    let result = this.equalizerPreset;
    result = 31 * result + this.bassBoostLevel;
    result = 31 * result + (this.isSurroundSoundEnabled ? 1 : 0);
    result = 31 * result + this.reverbPreset;
    for (const level of this.customEqualizerLevels) {
      result = 31 * result + Math.floor(level * 1000);
    }
    return result;
  }
}
