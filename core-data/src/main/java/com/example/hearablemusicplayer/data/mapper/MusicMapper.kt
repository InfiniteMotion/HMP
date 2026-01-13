package com.example.hearablemusicplayer.data.mapper

import com.example.hearablemusicplayer.data.database.Music as MusicEntity
import com.example.hearablemusicplayer.data.database.MusicExtra as MusicExtraEntity
import com.example.hearablemusicplayer.data.database.UserInfo as UserInfoEntity
import com.example.hearablemusicplayer.data.database.MusicInfo as MusicInfoEntity
import com.example.hearablemusicplayer.data.database.MusicLabel as MusicLabelEntity
import com.example.hearablemusicplayer.data.database.ListeningDuration as ListeningDurationEntity
import com.example.hearablemusicplayer.domain.model.Music
import com.example.hearablemusicplayer.domain.model.MusicExtra
import com.example.hearablemusicplayer.domain.model.UserInfo
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.MusicLabel
import com.example.hearablemusicplayer.domain.model.ListeningDuration
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory as DataLabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName as DataLabelName
import com.example.hearablemusicplayer.domain.model.enum.LabelCategory as DomainLabelCategory
import com.example.hearablemusicplayer.domain.model.enum.LabelName as DomainLabelName

// Music
fun MusicEntity.toDomain(): Music = Music(
    id = id,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    path = path,
    albumArtUri = albumArtUri
)

fun Music.toEntity(): MusicEntity = MusicEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    path = path,
    albumArtUri = albumArtUri
)

// MusicExtra
fun MusicExtraEntity.toDomain(): MusicExtra = MusicExtra(
    id = id,
    lyrics = lyrics,
    bitRate = bitRate,
    sampleRate = sampleRate,
    fileSize = fileSize,
    format = format,
    language = language,
    year = year,
    recommendationIds = recommendationIds,
    isGetExtraInfo = isGetExtraInfo,
    rewards = rewards,
    popLyric = popLyric,
    singerIntroduce = singerIntroduce,
    backgroundIntroduce = backgroundIntroduce,
    description = description,
    relevantMusic = relevantMusic
)

fun MusicExtra.toEntity(): MusicExtraEntity = MusicExtraEntity(
    id = id,
    lyrics = lyrics,
    bitRate = bitRate,
    sampleRate = sampleRate,
    fileSize = fileSize,
    format = format,
    language = language,
    year = year,
    recommendationIds = recommendationIds,
    isGetExtraInfo = isGetExtraInfo,
    rewards = rewards,
    popLyric = popLyric,
    singerIntroduce = singerIntroduce,
    backgroundIntroduce = backgroundIntroduce,
    description = description,
    relevantMusic = relevantMusic
)

// UserInfo
fun UserInfoEntity.toDomain(): UserInfo = UserInfo(
    id = id,
    liked = liked,
    disLiked = disLiked,
    lastPlayed = lastPlayed,
    playCount = playCount,
    skippedCount = skippedCount,
    userRating = userRating,
    inCustomPlaylistCount = inCustomPlaylistCount
)

fun UserInfo.toEntity(): UserInfoEntity = UserInfoEntity(
    id = id,
    liked = liked,
    disLiked = disLiked,
    lastPlayed = lastPlayed,
    playCount = playCount,
    skippedCount = skippedCount,
    userRating = userRating,
    inCustomPlaylistCount = inCustomPlaylistCount
)

// MusicInfo
fun MusicInfoEntity.toDomain(): MusicInfo = MusicInfo(
    music = music.toDomain(),
    extra = extra?.toDomain(),
    userInfo = userInfo?.toDomain()
)

fun MusicInfo.toEntity(): MusicInfoEntity = MusicInfoEntity(
    music = music.toEntity(),
    extra = extra?.toEntity(),
    userInfo = userInfo?.toEntity()
)

// MusicLabel
fun MusicLabelEntity.toDomain(): MusicLabel = MusicLabel(
    musicId = musicId,
    type = DomainLabelCategory.valueOf(type.name),
    label = DomainLabelName.valueOf(label.name)
)

fun MusicLabel.toEntity(): MusicLabelEntity = MusicLabelEntity(
    musicId = musicId,
    type = DataLabelCategory.valueOf(type.name),
    label = DataLabelName.valueOf(label.name)
)

// ListeningDuration
fun ListeningDurationEntity.toDomain(): ListeningDuration = ListeningDuration(
    date = date,
    duration = duration,
    updatedAt = updatedAt
)

fun ListeningDuration.toEntity(): ListeningDurationEntity = ListeningDurationEntity(
    date = date,
    duration = duration,
    updatedAt = updatedAt
)

// PlaybackHistory
fun com.example.hearablemusicplayer.data.database.PlaybackHistory.toDomain(): com.example.hearablemusicplayer.domain.model.PlaybackHistory = com.example.hearablemusicplayer.domain.model.PlaybackHistory(
    id = id,
    musicId = musicId,
    playedAt = playedAt,
    playDuration = playDuration,
    isCompleted = isCompleted,
    source = source
)

fun com.example.hearablemusicplayer.domain.model.PlaybackHistory.toEntity(): com.example.hearablemusicplayer.data.database.PlaybackHistory = com.example.hearablemusicplayer.data.database.PlaybackHistory(
    id = id,
    musicId = musicId,
    playedAt = playedAt,
    playDuration = playDuration,
    isCompleted = isCompleted,
    source = source
)
