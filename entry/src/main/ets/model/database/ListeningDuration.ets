/**
 * 收听时长记录模型
 */
export class ListeningDuration {
  date: string;  // 使用 yyyy-MM-dd 格式存储日期
  duration: number; // 以毫秒为单位存储时长
  updatedAt: number; // 最后更新时间

  constructor(
    date: string,
    duration: number,
    updatedAt: number = Date.now()
  ) {
    this.date = date;
    this.duration = duration;
    this.updatedAt = updatedAt;
  }
}
