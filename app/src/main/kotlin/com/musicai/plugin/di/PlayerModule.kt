package com.musicai.plugin.di

import com.musicai.plugin.audioPlayer.AudioPlayerFactory
import com.musicai.plugin.audioPlayer.MediaAudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideAudioPlayerFactory(): AudioPlayerFactory {
        return AudioPlayerFactory { MediaAudioPlayer() }
    }
}
