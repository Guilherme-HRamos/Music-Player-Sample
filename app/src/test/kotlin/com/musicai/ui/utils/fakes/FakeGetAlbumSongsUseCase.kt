package com.musicai.ui.utils.fakes

import com.musicai.domain.model.Album
import com.musicai.domain.usecase.GetAlbumSongsUseCase
import com.musicai.ui.utils.mocks.getAlbumWithEmptySongs

class FakeGetAlbumSongsUseCase : GetAlbumSongsUseCase {
    var invokeCalls = 0
    var lastCollectionId: Long? = null

    private var result: Result<Album> = Result.success(getAlbumWithEmptySongs(0L))

    fun setSuccess(album: Album) {
        result = Result.success(album)
    }

    fun setError(e: Throwable) {
        result = Result.failure(e)
    }

    override suspend fun invoke(collectionId: Long): Result<Album> {
        invokeCalls++
        lastCollectionId = collectionId
        return result
    }
}