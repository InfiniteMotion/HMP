/**
 * 播放列表基本信息模型
 */
export class Playlist {
  id: number = 0;
  name: string;

  constructor(
    name: string,
    id: number = 0
  ) {
    this.id = id;
    this.name = name;
  }
}
