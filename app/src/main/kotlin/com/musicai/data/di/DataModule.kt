package com.musicai.data.di

import android.content.Context
import androidx.room.Room
import com.musicai.data.api.local.MusicDatabase
import com.musicai.data.api.local.SongDao
import com.musicai.data.network.ItunesApiService
import com.musicai.data.repository.SongRepositoryImpl
import com.musicai.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    companion object {

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                },
            )
            .build()

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(ItunesApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        @Provides
        @Singleton
        fun provideItunesApiService(retrofit: Retrofit): ItunesApiService =
                retrofit.create(ItunesApiService::class.java)

        @Provides
        @Singleton
        fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase =
                Room.databaseBuilder(context, MusicDatabase::class.java, "music.db").build()

        @Provides
        @Singleton
        fun provideSongDao(db: MusicDatabase): SongDao = db.songDao()
    }
}
