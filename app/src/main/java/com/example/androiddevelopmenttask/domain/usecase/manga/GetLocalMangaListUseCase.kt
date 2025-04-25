package com.example.androiddevelopmenttask.domain.usecase.manga

import com.example.androiddevelopmenttask.domain.model.Manga
import com.example.androiddevelopmenttask.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalMangaListUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    operator fun invoke(): Flow<List<Manga>> {
        return mangaRepository.getLocalMangaList()
    }
}
