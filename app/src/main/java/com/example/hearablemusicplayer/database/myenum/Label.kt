package com.example.hearablemusicplayer.database.myenum

enum class LabelName {
    // 曲风类型
    ROCK, POP, JAZZ, CLASSICAL, HIPHOP, ELECTRONIC, FOLK, RNB,
    METAL, COUNTRY, BLUES, REGGAE, PUNK, FUNK, SOUL, INDIE,

    // 音乐情绪
    HAPPY, SAD, ENERGETIC, CALM, ROMANTIC, ANGRY, LONELY, UPLIFTING,
    MYSTERIOUS, DARK, MELANCHOLY, HOPEFUL,

    // 适用场景
    WORKOUT, SLEEP, PARTY, DRIVING, STUDY, RELAX, DINNER, MEDITATION,
    FOCUS, TRAVEL, MORNING, NIGHT,

    // 歌曲语言
    ENGLISH, CHINESE, JAPANESE, KOREAN, SPANISH, FRENCH, GERMAN,
    ITALIAN, ARABIC, HINDI, RUSSIAN,

    // 年代标签
    SIXTIES, SEVENTIES, EIGHTIES, NINETIES,
    TWO_THOUSANDS, TWENTY_TENS, TWENTY_TWENTIES,

    UNKNOWN;

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

