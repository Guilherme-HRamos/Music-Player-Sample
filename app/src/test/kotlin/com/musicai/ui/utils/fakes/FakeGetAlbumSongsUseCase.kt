package com.musicai.ui.utils.fakes

import com.musicai.domain.model.Album
import com.musicai.domain.usecase.GetAlbumSongsUseCase
import com.musicai.ui.utils.mocks.getAlbumWithEmptySongs

internal sealed class FakeGetAlbumSongsUseCase : GetAlbumSongsUseCase {
    data object Success : FakeGetAlbumSongsUseCase() {
        override suspend fun invoke(collectionId: Long): Result<Album> {
            return Result.success(getAlbumWithEmptySongs(collectionId))
        }
    }

    object Error : FakeGetAlbumSongsUseCase() {
        override suspend fun invoke(collectionId: Long): Result<Album> {
            return Result.failure(IllegalArgumentException())
        }
    }
}