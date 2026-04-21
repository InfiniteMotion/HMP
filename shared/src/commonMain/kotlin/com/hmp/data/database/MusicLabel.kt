package com.hmp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.datetime.Instant

@Entity(tableName = "music_label")
data class MusicLabel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "color") val color: String? = null,
    @ColumnInfo(name = "icon") val icon: String? = null
)

@Entity(tableName = "music_label_mapping")
data class MusicLabelMapping(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "music_id") val musicId: String,
    @ColumnInfo(name = "label_id") val labelId: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Dao
interface MusicLabelDao {
    @Insert
    suspend fun insert(label: MusicLabel)

    @Insert
    suspend fun insertAll(labels: List<MusicLabel>)

    @Update
    suspend fun update(label: MusicLabel)

    @Delete
    suspend fun delete(label: MusicLabel)

    @Query("SELECT * FROM music_label")
    suspend fun getAll(): List<MusicLabel>

    @Query("SELECT * FROM music_label WHERE id = :id")
    suspend fun getById(id: String): MusicLabel?

    @Query("SELECT * FROM music_label WHERE category = :category")
    suspend fun getByCategory(category: String): List<MusicLabel>

    @Query("SELECT * FROM music_label WHERE name LIKE :name")
    suspend fun searchByName(name: String): List<MusicLabel>
}

@Dao
interface MusicLabelMappingDao {
    @Insert
    suspend fun insert(mapping: MusicLabelMapping)

    @Insert
    suspend fun insertAll(mappings: List<MusicLabelMapping>)

    @Delete
    suspend fun delete(mapping: MusicLabelMapping)

    @Query("SELECT * FROM music_label_mapping WHERE music_id = :musicId")
    suspend fun getByMusicId(musicId: String): List<MusicLabelMapping>

    @Query("SELECT * FROM music_label_mapping WHERE label_id = :labelId")
    suspend fun getByLabelId(labelId: String): List<MusicLabelMapping>

    @Query("DELETE FROM music_label_mapping WHERE music_id = :musicId AND label_id = :labelId")
    suspend fun deleteByMusicAndLabel(musicId: String, labelId: String)

    @Query("SELECT ml.* FROM music_label ml JOIN music_label_mapping mlm ON ml.id = mlm.label_id WHERE mlm.music_id = :musicId")
    suspend fun getLabelsByMusicId(musicId: String): List<MusicLabel>

    @Query("SELECT m.* FROM music m JOIN music_label_mapping mlm ON m.id = mlm.music_id WHERE mlm.label_id = :labelId")
    suspend fun getMusicByLabelId(labelId: String): List<Music>
}