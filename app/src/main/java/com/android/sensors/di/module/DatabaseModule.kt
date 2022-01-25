package com.android.sensors.di.module

import android.content.Context
import androidx.room.Room
import com.android.sensors.data.local.AppDatabase
import com.android.sensors.utils.Const.APP_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        APP_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDao(database: AppDatabase) = database.sensorsDao()

    @Singleton
    @Provides
    fun provideRemoteKeysDao(database: AppDatabase) = database.remoteKeysDao()

}