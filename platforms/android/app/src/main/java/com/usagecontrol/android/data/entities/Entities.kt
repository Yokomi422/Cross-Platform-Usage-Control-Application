package com.usagecontrol.android.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "app_restrictions")
@Serializable
data class AppRestriction(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val dailyTimeLimit: Long = 0, // in milliseconds
    val usedTimeToday: Long = 0,
    val level: Int = 1, // Progressive level (1-5)
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "usage_sessions")
@Serializable
data class UsageSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val level: Int,
    val wasBlocked: Boolean = false,
    val isSynced: Boolean = false
)

@Entity(tableName = "progressive_levels")
@Serializable
data class ProgressiveLevel(
    @PrimaryKey val level: Int,
    val name: String,
    val description: String,
    val maxDailyUsage: Long, // in milliseconds
    val blockedCategories: List<String> = emptyList(),
    val requiresCompletion: Boolean = false,
    val completionCriteria: String = "",
    val isUnlocked: Boolean = false
)

@Entity(tableName = "user_settings")
@Serializable
data class UserSettings(
    @PrimaryKey val id: String = "default",
    val currentLevel: Int = 1,
    val enableStrictMode: Boolean = false,
    val allowEmergencyOverride: Boolean = true,
    val syncEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val lastSyncTime: Long = 0
)
