package com.hmp.data.database

actual abstract class AppDatabase : RoomDatabase() {
    actual abstract fun musicDao(): MusicDao
    actual abstract fun musicExtraDao(): MusicExtraDao
    actual abstract fun userInfoDao(): UserInfoDao
    actual abstract fun playlistDao(): PlaylistDao
    actual abstract fun listeningDurationDao(): ListeningDurationDao
    actual abstract fun musicLabelDao(): MusicLabelDao
    actual abstract fun musicLabelMappingDao(): MusicLabelMappingDao
    actual abstract fun playbackHistoryDao(): PlaybackHistoryDao

    actual companion object {
        private var instance: AppDatabase? = null

        actual fun getInstance(): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: object : AppDatabase() {
                    override fun musicDao(): MusicDao {
                        TODO("Not yet implemented")
                    }

                    override fun musicExtraDao(): MusicExtraDao {
                        TODO("Not yet implemented")
                    }

                    override fun userInfoDao(): UserInfoDao {
                        TODO("Not yet implemented")
                    }

                    override fun playlistDao(): PlaylistDao {
                        TODO("Not yet implemented")
                    }

                    override fun listeningDurationDao(): ListeningDurationDao {
                        TODO("Not yet implemented")
                    }

                    override fun musicLabelDao(): MusicLabelDao {
                        TODO("Not yet implemented")
                    }

                    override fun musicLabelMappingDao(): MusicLabelMappingDao {
                        TODO("Not yet implemented")
                    }

                    override fun playbackHistoryDao(): PlaybackHistoryDao {
                        TODO("Not yet implemented")
                    }
                }.also {
                    instance = it
                }
            }
        }
    }
}
