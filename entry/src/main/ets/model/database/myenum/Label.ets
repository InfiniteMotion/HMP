/**
 * 标签名称枚举
 */
export enum LabelName {
  // 曲风类型
  ROCK,
  POP,
  JAZZ,
  CLASSICAL,
  HIPHOP,
  ELECTRONIC,
  FOLK,
  RNB,
  METAL,
  COUNTRY,
  BLUES,
  REGGAE,
  PUNK,
  FUNK,
  SOUL,
  INDIE,

  // 音乐情绪
  HAPPY,
  SAD,
  ENERGETIC,
  CALM,
  ROMANTIC,
  ANGRY,
  LONELY,
  UPLIFTING,
  MYSTERIOUS,
  DARK,
  MELANCHOLY,
  HOPEFUL,

  // 适用场景
  WORKOUT,
  SLEEP,
  PARTY,
  DRIVING,
  STUDY,
  RELAX,
  DINNER,
  MEDITATION,
  FOCUS,
  TRAVEL,
  MORNING,
  NIGHT,

  // 歌曲语言
  ENGLISH,
  CHINESE,
  JAPANESE,
  KOREAN,
  SPANISH,
  FRENCH,
  GERMAN,
  ITALIAN,
  ARABIC,
  HINDI,
  RUSSIAN,

  // 年代标签
  SIXTIES,
  SEVENTIES,
  EIGHTIES,
  NINETIES,
  TWO_THOUSANDS,
  TWENTY_TENS,
  TWENTY_TWENTIES,

  UNKNOWN
}

/**
 * 标签分类枚举
 */
export enum LabelCategory {
  GENRE,
  MOOD,
  SCENARIO,
  LANGUAGE,
  ERA
}

/**
 * 标签工具类
 */
export class LabelUtils {
  /**
   * 根据字符串匹配标签名称
   * @param value 标签字符串
   * @returns 匹配的标签名称，未匹配到返回undefined
   */
  static match(value: string): LabelName | undefined {
    const keys = Object.keys(LabelName).filter(key => isNaN(Number(key)));
    for (const key of keys) {
      if (key.toLowerCase() === value.toLowerCase()) {
        return LabelName[key as string] as LabelName;
      }
    }
    return undefined;
  }

  /**
   * 获取标签对应的图标资源名称(小写)
   * UI层需要根据这个名称获取media资源
   * @param label 标签名称
   * @returns 图标资源名称
   */
  static getIconResourceName(label: LabelName): string {
    return LabelName[label].toLowerCase();
  }
}
