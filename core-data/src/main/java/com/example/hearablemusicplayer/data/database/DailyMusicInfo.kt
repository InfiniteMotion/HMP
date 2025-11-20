package com.example.hearablemusicplayer.data.database

data class DailyMusicInfo(
    val genre: List<String>,
    val mood: List<String>,
    val scenario:List<String>,
    val language:String,
    val era:String,
    val rewards:String,
    val lyric:String,
    val singerIntroduce:String,
    val backgroundIntroduce:String,
    val description:String,
    val relevantMusic:String,
    var errorInfo:String
)
