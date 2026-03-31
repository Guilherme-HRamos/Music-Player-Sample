package com.musicai.domain.di

import com.musicai.domain.usecase.GetAlbumSongsUseCase
import com.musicai.domain.usecase.GetAlbumSongsUseCaseImpl
import com.musicai.domain.usecase.GetRecentSongsUseCase
import com.musicai.domain.usecase.GetRecentSongsUseCaseImpl
import com.musicai.domain.usecase.SaveRecentSongUseCase
import com.musicai.domain.usecase.SaveRecentSongUseCaseImpl
import com.musicai.domain.usecase.SearchSongsUseCase
import com.musicai.domain.usecase.SearchSongsUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindSearchSongsUseCase(impl: SearchSongsUseCaseImpl): SearchSongsUseCase

    @Binds
    abstract fun bindGetRecentSongsUseCase(impl: GetRecentSongsUseCaseImpl): GetRecentSongsUseCase

    @Binds
    abstract fun bindGetAlbumSongsUseCase(impl: GetAlbumSongsUseCaseImpl): GetAlbumSongsUseCase

    @Binds
    abstract fun bindSaveRecentSongUseCase(impl: SaveRecentSongUseCaseImpl): SaveRecentSongUseCase
}
