package com.usagecontrol.android.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.usagecontrol.android.data.database.UsageControlDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideUsageControlDatabase(@ApplicationContext context: Context): UsageControlDatabase {
        return Room.databaseBuilder(
            context,
            UsageControlDatabase::class.java,
            "usage_control_database"
        ).build()
    }

    @Provides
    fun provideAppRestrictionDao(database: UsageControlDatabase) = database.appRestrictionDao()

    @Provides
    fun provideUsageSessionDao(database: UsageControlDatabase) = database.usageSessionDao()

    @Provides
    fun provideProgressiveLevelDao(database: UsageControlDatabase) = database.progressiveLevelDao()

    @Provides
    fun provideUserSettingsDao(database: UsageControlDatabase) = database.userSettingsDao()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
