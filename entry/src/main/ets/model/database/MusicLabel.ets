import { LabelCategory, LabelName } from './myenum/Label';

/**
 * 音乐标签关联模型
 */
export class MusicLabel {
  musicId: number;
  type: LabelCategory;
  label: LabelName;

  constructor(
    musicId: number,
    type: LabelCategory,
    label: LabelName
  ) {
    this.musicId = musicId;
    this.type = type;
    this.label = label;
  }
}
