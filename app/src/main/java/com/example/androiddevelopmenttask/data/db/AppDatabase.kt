package com.example.androiddevelopmenttask.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.androiddevelopmenttask.data.db.converter.StringListConverter
import com.example.androiddevelopmenttask.data.db.dao.MangaDao
import com.example.androiddevelopmenttask.data.db.dao.UserDao
import com.example.androiddevelopmenttask.data.db.entity.MangaEntity
import com.example.androiddevelopmenttask.data.db.entity.UserEntity

@Database(
    entities = [UserEntity::class, MangaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mangaDao(): MangaDao
}
