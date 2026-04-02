package com.musicai.plugin.di

import android.util.Log
import com.musicai.plugin.audioPlayer.AudioPlayerFactory
import com.musicai.plugin.audioPlayer.MediaAudioPlayer
import com.musicai.plugin.utils.LogCatLogger
import com.musicai.plugin.utils.Logger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {

    @Provides
    @Singleton
    fun provideLogCatLogger(): Logger {
        return LogCatLogger()
    }
}
