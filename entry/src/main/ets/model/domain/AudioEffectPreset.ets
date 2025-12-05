/**
 * 音效预设数据类
 * @param id 预设ID
 * @param name 预设名称
 * @param description 预设描述
 */
export class AudioEffectPreset {
  id: number;
  name: string;
  description: string;

  constructor(id: number, name: string, description: string) {
    this.id = id;
    this.name = name;
    this.description = description;
  }
}
