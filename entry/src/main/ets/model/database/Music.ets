/**
 * 音乐基础信息模型
 */
export class Music {
  id: number;
  title: string;
  artist: string;
  album: string;
  duration: number;
  path: string;
  albumArtUri: string;

  constructor(
    id: number,
    title: string,
    artist: string,
    album: string,
    duration: number,
    path: string,
    albumArtUri: string
  ) {
    this.id = id;
    this.title = title;
    this.artist = artist;
    this.album = album;
    this.duration = duration;
    this.path = path;
    this.albumArtUri = albumArtUri;
  }
}

/**
 * 音乐额外信息模型
 */
export class MusicExtra {
  id: number;
  lyrics: string | null = null;
  bitRate: number | null = null;           // 比特率 kbps
  sampleRate: number | null = null;        // 采样率 Hz
  fileSize: number | null = null;         // 文件大小 Byte
  format: string | null = null;         // 文件格式 mp3/flac
  language: string | null = null;       // 语言
  year: number | null = null;              // 年份
  recommendationIds: string | null = null;  // 推荐关联的音乐ID列表
  // 其他额外信息
  isGetExtraInfo: boolean;
  rewards: string | null = null;
  popLyric: string | null = null;
  singerIntroduce: string | null = null;
  backgroundIntroduce: string | null = null;
  description: string | null = null;
  relevantMusic: string | null = null;

  constructor(
    id: number,
    isGetExtraInfo: boolean,
    lyrics: string | null = null,
    bitRate: number | null = null,
    sampleRate: number | null = null,
    fileSize: number | null = null,
    format: string | null = null,
    language: string | null = null,
    year: number | null = null,
    recommendationIds: string | null = null,
    rewards: string | null = null,
    popLyric: string | null = null,
    singerIntroduce: string | null = null,
    backgroundIntroduce: string | null = null,
    description: string | null = null,
    relevantMusic: string | null = null
  ) {
    this.id = id;
    this.isGetExtraInfo = isGetExtraInfo;
    this.lyrics = lyrics;
    this.bitRate = bitRate;
    this.sampleRate = sampleRate;
    this.fileSize = fileSize;
    this.format = format;
    this.language = language;
    this.year = year;
    this.recommendationIds = recommendationIds;
    this.rewards = rewards;
    this.popLyric = popLyric;
    this.singerIntroduce = singerIntroduce;
    this.backgroundIntroduce = backgroundIntroduce;
    this.description = description;
    this.relevantMusic = relevantMusic;
  }
}

/**
 * 用户音乐信息模型
 */
export class UserInfo {
  id: number;
  liked: boolean = false;
  disLiked: boolean = false;
  lastPlayed: number | null = null;
  playCount: number | null = null;
  skippedCount: number | null = null;
  userRating: number | null = null;
  inCustomPlaylistCount: number | null = null;

  constructor(
    id: number,
    liked: boolean = false,
    disLiked: boolean = false,
    lastPlayed: number | null = null,
    playCount: number | null = null,
    skippedCount: number | null = null,
    userRating: number | null = null,
    inCustomPlaylistCount: number | null = null
  ) {
    this.id = id;
    this.liked = liked;
    this.disLiked = disLiked;
    this.lastPlayed = lastPlayed;
    this.playCount = playCount;
    this.skippedCount = skippedCount;
    this.userRating = userRating;
    this.inCustomPlaylistCount = inCustomPlaylistCount;
  }
}

/**
 * 完整音乐信息模型，包含基础信息、额外信息和用户信息
 */
export class MusicInfo {
  music: Music;
  extra: MusicExtra | null;
  userInfo: UserInfo | null;

  constructor(
    music: Music,
    extra: MusicExtra | null,
    userInfo: UserInfo | null
  ) {
    this.music = music;
    this.extra = extra;
    this.userInfo = userInfo;
  }
}
