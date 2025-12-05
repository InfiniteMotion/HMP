/**
 * 播放列表项模型，存储播放列表中的歌曲信息
 */
export class PlaylistItem {
  songUrl: string;
  songId: number;
  playlistId: number;

  constructor(
    songUrl: string,
    songId: number,
    playlistId: number
  ) {
    this.songUrl = songUrl;
    this.songId = songId;
    this.playlistId = playlistId;
  }
}
