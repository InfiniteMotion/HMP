/**
 * 数据库管理类，用于初始化和管理鸿蒙数据库
 */
import relationalStore from '@ohos.data.relationalStore';
import { UIAbility } from '@kit.AbilityKit';

// 数据库名称
const DB_NAME = 'music_player.db';
// 数据库版本
// const DB_VERSION = 1;

// 表名常量
export const TABLE_MUSIC = 'music';
export const TABLE_MUSIC_EXTRA = 'musicExtra';
export const TABLE_USER_INFO = 'userInfo';
export const TABLE_MUSIC_LABEL = 'musicLabel';
export const TABLE_PLAYLIST = 'playlist';
export const TABLE_PLAYLIST_ITEM = 'playlist_item';
export const TABLE_PLAYBACK_HISTORY = 'playbackHistory';
export const TABLE_LISTENING_DURATION = 'listeningDuration';

/**
 * 数据库管理类
 */
export class DatabaseManager {
  private static instance: DatabaseManager | null = null;
  private database: relationalStore.RdbStore | null = null;

  private constructor() {}

  /**
   * 获取数据库管理类实例
   */
  static getInstance(): DatabaseManager {
    if (!DatabaseManager.instance) {
      DatabaseManager.instance = new DatabaseManager();
    }
    return DatabaseManager.instance;
  }

  /**
   * 初始化数据库
   * @param context 应用上下文
   */
  async init(uiAbility: UIAbility): Promise<void> {
    if (this.database) {
      return;
    }

    try {
      const storeConfig: relationalStore.StoreConfig = {
        name: DB_NAME,
        securityLevel: relationalStore.SecurityLevel.S1,
        encrypt: false
      };

      this.database = await relationalStore.getRdbStore(uiAbility.context, storeConfig);
      
      // 创建数据表
      await this.createTables();
    } catch (error) {
      console.error('初始化数据库失败:', error);
      throw new Error(typeof error === 'string' ? error : JSON.stringify(error));
    }
  }

  /**
   * 创建所有数据表
   */
  private async createTables(): Promise<void> {
    if (!this.database) {
      throw new Error('数据库未初始化');
    }

    try {
      // 创建音乐表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_MUSIC} (
          id INTEGER PRIMARY KEY,
          title TEXT NOT NULL,
          artist TEXT NOT NULL,
          album TEXT NOT NULL,
          duration INTEGER NOT NULL,
          path TEXT NOT NULL,
          albumArtUri TEXT NOT NULL
        );
      `);

      // 创建音乐额外信息表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_MUSIC_EXTRA} (
          id INTEGER PRIMARY KEY,
          lyrics TEXT,
          bitRate INTEGER,
          sampleRate INTEGER,
          fileSize INTEGER,
          format TEXT,
          language TEXT,
          year INTEGER,
          recommendationIds TEXT,
          isGetExtraInfo INTEGER NOT NULL,
          rewards TEXT,
          popLyric TEXT,
          singerIntroduce TEXT,
          backgroundIntroduce TEXT,
          description TEXT,
          relevantMusic TEXT
        );
      `);

      // 创建用户信息表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_USER_INFO} (
          id INTEGER PRIMARY KEY,
          liked INTEGER NOT NULL DEFAULT 0,
          disLiked INTEGER NOT NULL DEFAULT 0,
          lastPlayed INTEGER,
          playCount INTEGER,
          skippedCount INTEGER,
          userRating INTEGER,
          inCustomPlaylistCount INTEGER
        );
      `);

      // 创建音乐标签表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_MUSIC_LABEL} (
          musicId INTEGER NOT NULL,
          type TEXT NOT NULL,
          label TEXT NOT NULL,
          PRIMARY KEY (musicId, label)
        );
      `);

      // 创建播放列表表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_PLAYLIST} (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          name TEXT NOT NULL
        );
      `);

      // 创建播放列表项表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_PLAYLIST_ITEM} (
          songUrl TEXT NOT NULL,
          songId INTEGER NOT NULL,
          playlistId INTEGER NOT NULL,
          PRIMARY KEY (songId, playlistId),
          FOREIGN KEY (playlistId) REFERENCES ${TABLE_PLAYLIST}(id) ON DELETE CASCADE
        );
      `);

      // 创建播放历史表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_PLAYBACK_HISTORY} (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          musicId INTEGER NOT NULL,
          playedAt INTEGER NOT NULL,
          playDuration INTEGER NOT NULL DEFAULT 0,
          isCompleted INTEGER NOT NULL DEFAULT 0,
          source TEXT
        );
      `);

      // 创建收听时长表
      await this.database.executeSql(`
        CREATE TABLE IF NOT EXISTS ${TABLE_LISTENING_DURATION} (
          date TEXT PRIMARY KEY,
          duration INTEGER NOT NULL,
          updatedAt INTEGER NOT NULL
        );
      `);

      console.info('所有数据表创建成功');
    } catch (error) {
      console.error('创建数据表失败:', error);
      throw new Error(typeof error === 'string' ? error : JSON.stringify(error));
    }
  }

  /**
   * 获取数据库实例
   */
  getDatabase(): relationalStore.RdbStore | null {
    return this.database;
  }

  /**
   * 关闭数据库
   */
  async close(): Promise<void> {
    if (this.database) {
      this.database.close().catch(() => {
        // TODO: Implement error handling.
      });
      this.database = null;
    }
  }
}
