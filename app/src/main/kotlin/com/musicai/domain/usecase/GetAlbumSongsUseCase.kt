package com.musicai.domain.usecase

import com.musicai.domain.model.Album
import com.musicai.domain.repository.SongRepository
import javax.inject.Inject

interface GetAlbumSongsUseCase {
    suspend operator fun invoke(collectionId: Long): Result<Album>
}

class GetAlbumSongsUseCaseImpl @Inject constructor(
    private val repository: SongRepository,
) : GetAlbumSongsUseCase {
    override suspend fun invoke(collectionId: Long): Result<Album> {
        return repository.getAlbumSongs(collectionId).map { songs ->
            val first = songs.firstOrNull()
            Album(
                collectionId = collectionId,
                collectionName = first?.collectionName ?: "",
                artistName = first?.artistName ?: "",
                artworkUrl = first?.artworkUrl ?: "",
                songs = songs,
            )
        }
    }
}
