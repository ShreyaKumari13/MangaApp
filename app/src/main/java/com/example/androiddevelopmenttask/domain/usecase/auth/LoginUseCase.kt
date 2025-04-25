package com.example.androiddevelopmenttask.domain.usecase.auth

import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.model.User
import com.example.androiddevelopmenttask.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error("Email and password cannot be empty")
        }
        
        return userRepository.loginUser(email, password)
    }
}
