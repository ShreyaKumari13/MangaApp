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
    version = 3,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mangaDao(): MangaDao

    companion object {
        // Migration from version 1 to version 2
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create a new table with the updated schema
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mangas_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        subTitle TEXT NOT NULL,
                        description TEXT NOT NULL,
                        coverImage TEXT NOT NULL,
                        authors TEXT NOT NULL,
                        genres TEXT NOT NULL,
                        chapters INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        rating REAL NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL,
                        page INTEGER NOT NULL
                    )
                    """
                )

                // Copy data from the old table to the new table
                database.execSQL(
                    """
                    INSERT INTO mangas_new (
                        id, title, subTitle, description, coverImage, 
                        authors, genres, chapters, status, rating, 
                        createdAt, updatedAt, page
                    )
                    SELECT 
                        id, title, '', description, coverImage,
                        '[]', genres, chapters, status, 0.0,
                        '', '', page 
                    FROM mangas
                    """
                )

                // Remove the old table
                database.execSQL("DROP TABLE IF EXISTS mangas")

                // Rename the new table to the original name
                database.execSQL("ALTER TABLE mangas_new RENAME TO mangas")
            }
        }

        // Migration from version 2 to version 3
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // First, check if the table exists
                database.execSQL("DROP TABLE IF EXISTS mangas_new")

                // Create a new table with the updated schema
                database.execSQL(
                    """
                    CREATE TABLE mangas_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        subTitle TEXT NOT NULL,
                        description TEXT NOT NULL,
                        coverImage TEXT NOT NULL,
                        authors TEXT NOT NULL,
                        genres TEXT NOT NULL,
                        chapters INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        nsfw INTEGER NOT NULL DEFAULT 0,
                        type TEXT NOT NULL DEFAULT 'japan',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        page INTEGER NOT NULL
                    )
                    """
                )

                // Copy data from the old table to the new table with default values for new columns
                database.execSQL(
                    """
                    INSERT INTO mangas_new (
                        id, title, subTitle, description, coverImage,
                        authors, genres, chapters, status,
                        nsfw, type, createdAt, updatedAt, page
                    )
                    SELECT 
                        id, title, subTitle, description, coverImage,
                        authors, genres, chapters, status,
                        0, 'japan',
                        CAST((CASE WHEN createdAt = '' THEN strftime('%s', 'now') ELSE strftime('%s', createdAt) END) AS INTEGER) * 1000,
                        CAST((CASE WHEN updatedAt = '' THEN strftime('%s', 'now') ELSE strftime('%s', updatedAt) END) AS INTEGER) * 1000,
                        page
                    FROM mangas
                    """
                )

                // Remove the old table
                database.execSQL("DROP TABLE IF EXISTS mangas")

                // Rename the new table to the original name
                database.execSQL("ALTER TABLE mangas_new RENAME TO mangas")
            }
        }
    }
}
