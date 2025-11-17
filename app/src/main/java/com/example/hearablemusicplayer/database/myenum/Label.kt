package com.example.hearablemusicplayer.database.myenum

import androidx.room.TypeConverter
import com.example.hearablemusicplayer.R

enum class LabelName(val iconResId: Int = R.drawable.unknown) {
    // 曲风类型
    ROCK(R.drawable.rock),
    POP(R.drawable.pop),
    JAZZ(R.drawable.jazz),
    CLASSICAL(R.drawable.classical),
    HIPHOP(R.drawable.hiphop),
    ELECTRONIC(R.drawable.electronic),
    FOLK(R.drawable.folk),
    RNB(R.drawable.rnb),
    METAL(R.drawable.metal),
    COUNTRY(R.drawable.country),
    BLUES(R.drawable.blues),
    REGGAE(R.drawable.reggae),
    PUNK(R.drawable.punk),
    FUNK(R.drawable.funk),
    SOUL(R.drawable.soul),
    INDIE(R.drawable.indie),

    // 音乐情绪
    HAPPY(R.drawable.happy),
    SAD(R.drawable.sad),
    ENERGETIC(R.drawable.energetic),
    CALM(R.drawable.calm),
    ROMANTIC(R.drawable.romantic),
    ANGRY(R.drawable.angry),
    LONELY(R.drawable.lonely),
    UPLIFTING(R.drawable.uplifting),
    MYSTERIOUS(R.drawable.mysterious),
    DARK(R.drawable.dark),
    MELANCHOLY(R.drawable.melancholy),
    HOPEFUL(R.drawable.hopeful),

    // 适用场景
    WORKOUT(R.drawable.workout),
    SLEEP(R.drawable.sleep),
    PARTY(R.drawable.party),
    DRIVING(R.drawable.driving),
    STUDY(R.drawable.study),
    RELAX(R.drawable.relax),
    DINNER(R.drawable.dinner),
    MEDITATION(R.drawable.meditation),
    FOCUS(R.drawable.focus),
    TRAVEL(R.drawable.travel),
    MORNING(R.drawable.morning),
    NIGHT(R.drawable.night),

    // 歌曲语言
    ENGLISH(R.drawable.english),
    CHINESE(R.drawable.chinese),
    JAPANESE(R.drawable.japanese),
    KOREAN(R.drawable.korean),
    SPANISH(R.drawable.spanish),
    FRENCH(R.drawable.french),
    GERMAN(R.drawable.german),
    ITALIAN(R.drawable.italian),
    ARABIC(R.drawable.arabic),
    HINDI(R.drawable.hindi),
    RUSSIAN(R.drawable.russian),


    // 年代标签
    SIXTIES(R.drawable.sixties),
    SEVENTIES(R.drawable.seventies),
    EIGHTIES(R.drawable.eighties),
    NINETIES(R.drawable.nineties),
    TWO_THOUSANDS(R.drawable.two_thousands),
    TWENTY_TENS(R.drawable.twenty_tens),
    TWENTY_TWENTIES(R.drawable.twenty_twenties),

    UNKNOWN(R.drawable.unknown);

    companion object {
        fun match(value: String): LabelName? {
            return enumValues<LabelName>().firstOrNull {
                it.name.equals(value, ignoreCase = true)
            }
        }
    }
}

enum class LabelCategory {
    GENRE, MOOD, SCENARIO, LANGUAGE, ERA
}

// LabelCategory,LabelName 转换器
class LabelConverters {
    @TypeConverter
    fun fromLabelCategory(category: LabelCategory): String {
        return category.name
    }
    @TypeConverter
    fun toLabelCategory(value: String): LabelCategory {
        return LabelCategory.valueOf(value)
    }
    @TypeConverter
    fun fromLabelName(label: LabelName): String {
        return label.name
    }
    @TypeConverter
    fun toLabelName(value: String): LabelName {
        return LabelName.valueOf(value)
    }
}