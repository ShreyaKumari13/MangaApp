package com.example.androiddevelopmenttask.di

import com.example.androiddevelopmenttask.data.repository.FaceDetectionRepositoryImpl
import com.example.androiddevelopmenttask.data.repository.MangaRepositoryImpl
import com.example.androiddevelopmenttask.data.repository.UserRepositoryImpl
import com.example.androiddevelopmenttask.domain.repository.FaceDetectionRepository
import com.example.androiddevelopmenttask.domain.repository.MangaRepository
import com.example.androiddevelopmenttask.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindMangaRepository(
        mangaRepositoryImpl: MangaRepositoryImpl
    ): MangaRepository

    @Binds
    @Singleton
    abstract fun bindFaceDetectionRepository(
        faceDetectionRepositoryImpl: FaceDetectionRepositoryImpl
    ): FaceDetectionRepository
}
