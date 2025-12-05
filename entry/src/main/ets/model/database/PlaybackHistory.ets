/**
 * 用户每次播放音乐的记录实体类
 */
export class PlaybackHistory {
  id: number = 0;                 // 自动生成的主键,用于标识唯一记录
  musicId: number;               // 播放的音乐的 ID(外键,关联 Music 表)
  playedAt: number;              // 播放时间戳,使用 Date.now() 获取
  playDuration: number = 0;      // 播放时长(单位:毫秒),可用于分析用户是否听完整
  isCompleted: boolean = false;  // 是否完整播放(例如播放超过 90% 可认为完整)
  source: string | null = null;  // 播放来源:推荐、搜索、播放列表、收藏等

  constructor(
    musicId: number,
    playedAt: number,
    playDuration: number = 0,
    isCompleted: boolean = false,
    source: string | null = null
  ) {
    this.musicId = musicId;
    this.playedAt = playedAt;
    this.playDuration = playDuration;
    this.isCompleted = isCompleted;
    this.source = source;
  }
}
