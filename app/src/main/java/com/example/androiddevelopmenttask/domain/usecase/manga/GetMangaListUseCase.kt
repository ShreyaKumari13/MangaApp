package com.example.androiddevelopmenttask.domain.usecase.manga

import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.repository.MangaRepository
import javax.inject.Inject

class GetMangaListUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int): Result<List<Manga>> {
        return mangaRepository.getMangaList(page, pageSize)
    }
}
