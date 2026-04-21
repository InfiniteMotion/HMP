package com.hmp.data.database

import android.content.Context
import androidx.room.Room

fun getDatabaseBuilder(context: Context): androidx.room.RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("music_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    ).fallbackToDestructiveMigration()
}
