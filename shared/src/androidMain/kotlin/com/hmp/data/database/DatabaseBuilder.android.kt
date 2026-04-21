package com.hmp.data.database

import android.content.Context
import androidx.room.Room

actual fun provideDatabaseBuilder(context: Any): AppDatabase.Builder {
    return Room.databaseBuilder(
        context as Context,
        AppDatabase::class.java,
        "hmp_database"
    )
}
