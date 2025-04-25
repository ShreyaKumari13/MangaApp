package com.example.androiddevelopmenttask.util

import android.util.Log
import com.example.androiddevelopmenttask.data.db.dao.MangaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to help inspect database contents for debugging
 */
@Singleton
class DatabaseInspector @Inject constructor(
    private val mangaDao: MangaDao
) {
    private val TAG = "DatabaseInspector"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun inspectMangaDatabase() {
        scope.launch {
            try {
                // Get total count
                val allMangas = mangaDao.getAllMangas().firstOrNull() ?: emptyList()
                Log.d(TAG, "Total manga in database: ${allMangas.size}")
                
                // Get max page
                val maxPage = mangaDao.getMaxPage() ?: 0
                Log.d(TAG, "Max page in database: $maxPage")
                
                // Get count per page
                for (page in 1..maxPage) {
                    val mangasForPage = mangaDao.getMangasByPage(page).firstOrNull() ?: emptyList()
                    Log.d(TAG, "Manga count for page $page: ${mangasForPage.size}")
                    
                    // Print first manga title from each page
                    if (mangasForPage.isNotEmpty()) {
                        Log.d(TAG, "First manga on page $page: ${mangasForPage.first().title}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inspecting database: ${e.message}")
            }
        }
    }
}
