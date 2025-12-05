/**
 * 均衡器频段数据类
 * @param bandIndex 频段索引
 * @param frequency 频段中心频率（Hz）
 * @param level 当前增益值（dB）
 * @param minLevel 最小增益值（dB）
 * @param maxLevel 最大增益值（dB）
 */
export class EqualizerBand {
  bandIndex: number;
  frequency: number;
  level: number;
  minLevel: number;
  maxLevel: number;

  constructor(
    bandIndex: number,
    frequency: number,
    level: number,
    minLevel: number,
    maxLevel: number
  ) {
    this.bandIndex = bandIndex;
    this.frequency = frequency;
    this.level = level;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
  }
}
