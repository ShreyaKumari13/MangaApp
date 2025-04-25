package com.example.androiddevelopmenttask.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.androiddevelopmenttask.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val password: String,
    val faceData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (email != other.email) return false
        if (password != other.password) return false
        if (faceData != null) {
            if (other.faceData == null) return false
            if (!faceData.contentEquals(other.faceData)) return false
        } else if (other.faceData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + (faceData?.contentHashCode() ?: 0)
        return result
    }
    
    fun toUser(): User {
        return User(
            id = id,
            email = email,
            password = password
        )
    }
    
    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                email = user.email,
                password = user.password
            )
        }
    }
}
