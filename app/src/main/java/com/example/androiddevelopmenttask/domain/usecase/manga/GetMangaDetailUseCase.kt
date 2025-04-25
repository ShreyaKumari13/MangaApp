package com.example.androiddevelopmenttask.domain.usecase.manga

import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.repository.MangaRepository
import javax.inject.Inject

class GetMangaDetailUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    suspend operator fun invoke(id: Int): Result<Manga> {
        return mangaRepository.getMangaById(id)
    }
}
