package com.example.androiddevelopmenttask.domain.repository

import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun registerUser(user: User): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<User>
    suspend fun getCurrentUser(): Flow<User?>
    suspend fun logoutUser()
}
