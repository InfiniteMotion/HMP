package com.example.hearablemusicplayer.data.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName
import kotlinx.coroutines.flow.Flow

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

    @Query("""
    SELECT label 
    FROM musicLabel 
    WHERE type = :type 
    GROUP BY label 
    ORDER BY COUNT(*) DESC
""")
    fun getLabelsByType(type: LabelCategory): Flow<List<LabelName>>

    @Query("""
    SELECT musicId 
    FROM musicLabel 
    WHERE label = :label
""")
    suspend fun getMusicIdListByType(label: LabelName): List<Long>
}
