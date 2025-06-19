package com.usagecontrol.android.data.database

import androidx.room.*
import com.usagecontrol.android.data.dao.*
import com.usagecontrol.android.data.entities.*

@TypeConverters(Converters::class)
@Database(
    entities = [
        AppRestriction::class,
        UsageSession::class,
        ProgressiveLevel::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UsageControlDatabase : RoomDatabase() {
    abstract fun appRestrictionDao(): AppRestrictionDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun progressiveLevelDao(): ProgressiveLevelDao
    abstract fun userSettingsDao(): UserSettingsDao
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
