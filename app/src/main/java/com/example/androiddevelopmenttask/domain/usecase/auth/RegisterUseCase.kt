package com.example.androiddevelopmenttask.domain.usecase.auth

import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.model.User
import com.example.androiddevelopmenttask.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error("Email and password cannot be empty")
        }
        
        if (!isValidEmail(email)) {
            return Result.Error("Invalid email format")
        }
        
        if (password.length < 6) {
            return Result.Error("Password must be at least 6 characters")
        }
        
        val user = User(email = email, password = password)
        return userRepository.registerUser(user)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
