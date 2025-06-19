package com.usagecontrol.android.data.dao

import androidx.room.*
import com.usagecontrol.android.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRestrictionDao {
    @Query("SELECT * FROM app_restrictions ORDER BY appName ASC")
    fun getAllRestrictions(): Flow<List<AppRestriction>>

    @Query("SELECT * FROM app_restrictions WHERE packageName = :packageName")
    suspend fun getRestriction(packageName: String): AppRestriction?

    @Query("SELECT * FROM app_restrictions WHERE isBlocked = 1")
    fun getBlockedApps(): Flow<List<AppRestriction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestriction(restriction: AppRestriction)

    @Update
    suspend fun updateRestriction(restriction: AppRestriction)

    @Delete
    suspend fun deleteRestriction(restriction: AppRestriction)

    @Query("UPDATE app_restrictions SET usedTimeToday = :usedTime, lastUpdated = :timestamp WHERE packageName = :packageName")
    suspend fun updateUsageTime(packageName: String, usedTime: Long, timestamp: Long)

    @Query("UPDATE app_restrictions SET usedTimeToday = 0")
    suspend fun resetDailyUsage()
}

@Dao
interface UsageSessionDao {
    @Query("SELECT * FROM usage_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<UsageSession>>

    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName ORDER BY startTime DESC")
    fun getSessionsForApp(packageName: String): Flow<List<UsageSession>>

    @Query("SELECT * FROM usage_sessions WHERE startTime >= :startTime")
    suspend fun getSessionsSince(startTime: Long): List<UsageSession>

    @Insert
    suspend fun insertSession(session: UsageSession)

    @Query("DELETE FROM usage_sessions WHERE startTime < :timestamp")
    suspend fun deleteOldSessions(timestamp: Long)

    @Query("SELECT * FROM usage_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<UsageSession>

    @Query("UPDATE usage_sessions SET isSynced = 1 WHERE id IN (:sessionIds)")
    suspend fun markSessionsAsSynced(sessionIds: List<Long>)
}

@Dao
interface ProgressiveLevelDao {
    @Query("SELECT * FROM progressive_levels ORDER BY level ASC")
    fun getAllLevels(): Flow<List<ProgressiveLevel>>

    @Query("SELECT * FROM progressive_levels WHERE level = :level")
    suspend fun getLevel(level: Int): ProgressiveLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: ProgressiveLevel)

    @Update
    suspend fun updateLevel(level: ProgressiveLevel)

    @Query("UPDATE progressive_levels SET isUnlocked = 1 WHERE level = :level")
    suspend fun unlockLevel(level: Int)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 'default' LIMIT 1")
    fun getSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)

    @Query("UPDATE user_settings SET currentLevel = :level WHERE id = 'default'")
    suspend fun updateCurrentLevel(level: Int)

    @Query("UPDATE user_settings SET lastSyncTime = :timestamp WHERE id = 'default'")
    suspend fun updateLastSyncTime(timestamp: Long)
}
