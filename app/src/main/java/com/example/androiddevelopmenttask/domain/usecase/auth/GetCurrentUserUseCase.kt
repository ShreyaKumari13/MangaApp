package com.example.androiddevelopmenttask.domain.usecase.auth

import com.example.androiddevelopmenttask.domain.model.User
import com.example.androiddevelopmenttask.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUser()
    }
}
