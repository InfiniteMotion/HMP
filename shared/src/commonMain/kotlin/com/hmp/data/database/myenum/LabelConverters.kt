package com.hmp.data.database.myenum

import androidx.room.TypeConverter

class LabelConverters {
    @TypeConverter
    fun fromLabelCategory(category: LabelCategory): String {
        return category.name
    }

    @TypeConverter
    fun toLabelCategory(category: String): LabelCategory {
        return LabelCategory.valueOf(category)
    }

    @TypeConverter
    fun fromLabelName(name: LabelName): String {
        return name.name
    }

    @TypeConverter
    fun toLabelName(name: String): LabelName {
        return LabelName.valueOf(name)
    }
}
