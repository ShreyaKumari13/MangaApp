package com.example.androiddevelopmenttask.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.androiddevelopmenttask.data.db.dao.UserDao
import com.example.androiddevelopmenttask.data.db.entity.UserEntity
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.model.User
import com.example.androiddevelopmenttask.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>
) : UserRepository {

    private val USER_ID_KEY = intPreferencesKey("user_id")

    override suspend fun registerUser(user: User): Result<Unit> {
        return try {
            val existingUser = userDao.getUserByEmail(user.email)
            if (existingUser != null) {
                Result.Error("User with this email already exists")
            } else {
                userDao.insertUser(UserEntity.fromUser(user))
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val user = userDao.loginUser(email, password)
            if (user != null) {
                // Save user ID to DataStore
                dataStore.edit { preferences ->
                    preferences[USER_ID_KEY] = user.id
                }
                Result.Success(user.toUser())
            } else {
                Result.Error("Invalid email or password")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getCurrentUser(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val userId = preferences[USER_ID_KEY]
            userId
        }.map { userId ->
            if (userId != null) {
                val user = userDao.getUserById(userId)
                user?.toUser()
            } else {
                null
            }
        }
    }

    override suspend fun logoutUser() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
}
