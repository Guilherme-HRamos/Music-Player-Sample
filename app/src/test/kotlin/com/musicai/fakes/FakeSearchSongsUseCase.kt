package com.musicai.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.SearchSongsUseCase

class FakeSearchSongsUseCase : SearchSongsUseCase {

    var result: Result<List<Song>> = Result.success(emptyList())

    /**
     * Queue de resultados para simular respostas sequenciais distintas (ex: página 0 e página 1).
     * Quando não vazio, consome o próximo item em vez de usar [result].
     */
    val resultsQueue: ArrayDeque<Result<List<Song>>> = ArrayDeque()

    var callCount = 0
    var lastQuery: String? = null
    var lastPage: Int? = null

    override suspend fun invoke(query: String, page: Int): Result<List<Song>> {
        callCount++
        lastQuery = query
        lastPage = page
        return if (resultsQueue.isNotEmpty()) resultsQueue.removeFirst() else result
    }
}
