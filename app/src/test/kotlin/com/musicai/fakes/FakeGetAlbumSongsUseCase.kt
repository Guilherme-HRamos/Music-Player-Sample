package com.musicai.fakes

import com.musicai.domain.model.Album
import com.musicai.domain.usecase.GetAlbumSongsUseCase

class FakeGetAlbumSongsUseCase : GetAlbumSongsUseCase {

    var result: Result<Album> = Result.success(Album(0L, "", "", "", emptyList()))
    var capturedCollectionId: Long? = null

    override suspend fun invoke(collectionId: Long): Result<Album> {
        capturedCollectionId = collectionId
        return result
    }
}
