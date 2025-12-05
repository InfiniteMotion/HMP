/**
 * 每日音乐推荐信息模型
 */
export class DailyMusicInfo {
  genre: string[];
  mood: string[];
  scenario: string[];
  language: string;
  era: string;
  rewards: string;
  lyric: string;
  singerIntroduce: string;
  backgroundIntroduce: string;
  description: string;
  relevantMusic: string;
  errorInfo: string;

  constructor(
    genre: string[],
    mood: string[],
    scenario: string[],
    language: string,
    era: string,
    rewards: string,
    lyric: string,
    singerIntroduce: string,
    backgroundIntroduce: string,
    description: string,
    relevantMusic: string,
    errorInfo: string
  ) {
    this.genre = genre;
    this.mood = mood;
    this.scenario = scenario;
    this.language = language;
    this.era = era;
    this.rewards = rewards;
    this.lyric = lyric;
    this.singerIntroduce = singerIntroduce;
    this.backgroundIntroduce = backgroundIntroduce;
    this.description = description;
    this.relevantMusic = relevantMusic;
    this.errorInfo = errorInfo;
  }
}
