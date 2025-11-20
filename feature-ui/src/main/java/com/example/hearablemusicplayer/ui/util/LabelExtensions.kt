package com.example.hearablemusicplayer.ui.util

import com.example.hearablemusicplayer.data.database.myenum.LabelName
import com.example.hearablemusicplayer.ui.R

/**
 * LabelName扩展属性,获取对应的drawable资源ID
 */
val LabelName.iconResId: Int
    get() = when (this) {
        // 曲风类型
        LabelName.ROCK -> R.drawable.rock
        LabelName.POP -> R.drawable.pop
        LabelName.JAZZ -> R.drawable.jazz
        LabelName.CLASSICAL -> R.drawable.classical
        LabelName.HIPHOP -> R.drawable.hiphop
        LabelName.ELECTRONIC -> R.drawable.electronic
        LabelName.FOLK -> R.drawable.folk
        LabelName.RNB -> R.drawable.rnb
        LabelName.METAL -> R.drawable.metal
        LabelName.COUNTRY -> R.drawable.country
        LabelName.BLUES -> R.drawable.blues
        LabelName.REGGAE -> R.drawable.reggae
        LabelName.PUNK -> R.drawable.punk
        LabelName.FUNK -> R.drawable.funk
        LabelName.SOUL -> R.drawable.soul
        LabelName.INDIE -> R.drawable.indie
        
        // 音乐情绪
        LabelName.HAPPY -> R.drawable.happy
        LabelName.SAD -> R.drawable.sad
        LabelName.ENERGETIC -> R.drawable.energetic
        LabelName.CALM -> R.drawable.calm
        LabelName.ROMANTIC -> R.drawable.romantic
        LabelName.ANGRY -> R.drawable.angry
        LabelName.LONELY -> R.drawable.lonely
        LabelName.UPLIFTING -> R.drawable.uplifting
        LabelName.MYSTERIOUS -> R.drawable.mysterious
        LabelName.DARK -> R.drawable.dark
        LabelName.MELANCHOLY -> R.drawable.melancholy
        LabelName.HOPEFUL -> R.drawable.hopeful
        
        // 适用场景
        LabelName.WORKOUT -> R.drawable.workout
        LabelName.SLEEP -> R.drawable.sleep
        LabelName.PARTY -> R.drawable.party
        LabelName.DRIVING -> R.drawable.driving
        LabelName.STUDY -> R.drawable.study
        LabelName.RELAX -> R.drawable.relax
        LabelName.DINNER -> R.drawable.dinner
        LabelName.MEDITATION -> R.drawable.meditation
        LabelName.FOCUS -> R.drawable.focus
        LabelName.TRAVEL -> R.drawable.travel
        LabelName.MORNING -> R.drawable.morning
        LabelName.NIGHT -> R.drawable.night
        
        // 歌曲语言
        LabelName.ENGLISH -> R.drawable.english
        LabelName.CHINESE -> R.drawable.chinese
        LabelName.JAPANESE -> R.drawable.japanese
        LabelName.KOREAN -> R.drawable.korean
        LabelName.SPANISH -> R.drawable.spanish
        LabelName.FRENCH -> R.drawable.french
        LabelName.GERMAN -> R.drawable.german
        LabelName.ITALIAN -> R.drawable.italian
        LabelName.ARABIC -> R.drawable.arabic
        LabelName.HINDI -> R.drawable.hindi
        LabelName.RUSSIAN -> R.drawable.russian
        
        // 年代标签
        LabelName.SIXTIES -> R.drawable.sixties
        LabelName.SEVENTIES -> R.drawable.seventies
        LabelName.EIGHTIES -> R.drawable.eighties
        LabelName.NINETIES -> R.drawable.nineties
        LabelName.TWO_THOUSANDS -> R.drawable.two_thousands
        LabelName.TWENTY_TENS -> R.drawable.twenty_tens
        LabelName.TWENTY_TWENTIES -> R.drawable.twenty_twenties
        
        LabelName.UNKNOWN -> R.drawable.unknown
    }
