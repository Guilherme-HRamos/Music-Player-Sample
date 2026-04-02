package com.musicai.ui.utils.fakes

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.usecase.SearchSongsUseCase

class FakeSearchSongsUseCase : SearchSongsUseCase {
    var invokeCalls = 0
    var lastQuery: String? = null
    var lastPage: Int? = null

    private var result: Result<PaginatedSearch> = Result.success(PaginatedSearch(emptyList(), false))

    fun setSuccess(paginated: PaginatedSearch) {
        result = Result.success(paginated)
    }

    fun setError(e: Throwable) {
        result = Result.failure(e)
    }

    override suspend fun invoke(query: String, page: Int): Result<PaginatedSearch> {
        invokeCalls++
        lastQuery = query
        lastPage = page
        return result
    }
}
