package com.example.hearablemusicplayer.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hearablemusicplayer.database.myenum.LabelCategory
import com.example.hearablemusicplayer.database.myenum.LabelName

@Entity(
    tableName = "musicLabel",
    primaryKeys = ["musicId", "label"]
)
data class MusicLabel(
    val musicId: Long,
    val type: LabelCategory,
    val label: LabelName
)


@Dao
interface MusicLabelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: MusicLabel)

    @Query("SELECT * FROM musicLabel WHERE musicId = :musicId")
    suspend fun getLabelsById(musicId: Long): List<MusicLabel>

}