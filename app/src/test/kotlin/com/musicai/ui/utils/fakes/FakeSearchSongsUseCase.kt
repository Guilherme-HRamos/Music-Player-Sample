package com.musicai.ui.utils.fakes

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.usecase.SearchSongsUseCase

internal sealed class FakeSearchSongsUseCase : SearchSongsUseCase {
    data class Success(var result: PaginatedSearch = PaginatedSearch(emptyList(), false)) : FakeSearchSongsUseCase() {
        override suspend fun invoke(query: String, page: Int): Result<PaginatedSearch> =
            Result.success(result)
    }

    data object Empty : FakeSearchSongsUseCase() {
        override suspend fun invoke(query: String, page: Int): Result<PaginatedSearch> =
            Result.success(PaginatedSearch(emptyList(), false))
    }

    data class Error(val throwable: Throwable = IllegalArgumentException()) : FakeSearchSongsUseCase() {
        override suspend fun invoke(query: String, page: Int): Result<PaginatedSearch> =
            Result.failure(throwable)
    }
}
