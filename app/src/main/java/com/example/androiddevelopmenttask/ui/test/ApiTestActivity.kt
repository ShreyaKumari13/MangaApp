package com.example.androiddevelopmenttask.ui.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.androiddevelopmenttask.data.api.MangaApiService
import com.example.androiddevelopmenttask.domain.model.Manga
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApiTestActivity : ComponentActivity() {

    @Inject
    lateinit var mangaApiService: MangaApiService

    private val TAG = "ApiTestActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ApiTestScreen(
                    onFetchManga = { fetchManga() },
                    onFetchLatest = { fetchLatestManga() },
                    onSearchManga = { searchManga("action") }
                )
            }
        }
    }

    private fun fetchManga(): List<Manga> {
        var mangaList = emptyList<Manga>()

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching manga list...")
                val response = mangaApiService.getMangaList(page = 1, limit = 10)

                // Log detailed response information
                Log.d(TAG, "API Response: code=${response.code}, items=${response.data.size}")

                if (response.data.isNotEmpty()) {
                    val firstItem = response.data.first()
                    Log.d(TAG, "First item: id=${firstItem.idString}, title=${firstItem.title}")
                    Log.d(TAG, "First item genres: ${firstItem.genres.joinToString(", ")}")
                }

                mangaList = response.data.map { it.toManga() }
                Log.d(TAG, "Fetched ${mangaList.size} manga")
                Toast.makeText(this@ApiTestActivity, "Fetched ${mangaList.size} manga", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching manga: ${e.message}", e)
                Log.e(TAG, "Error details:", e)
                Toast.makeText(this@ApiTestActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return mangaList
    }

    private fun fetchLatestManga(): List<Manga> {
        var mangaList = emptyList<Manga>()

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching latest manga...")
                val response = mangaApiService.getLatestManga()

                // Log detailed response information
                Log.d(TAG, "API Response: code=${response.code}, items=${response.data.size}")

                if (response.data.isNotEmpty()) {
                    val firstItem = response.data.first()
                    Log.d(TAG, "First latest item: id=${firstItem.idString}, title=${firstItem.title}")
                }

                mangaList = response.data.map { it.toManga() }
                Log.d(TAG, "Fetched ${mangaList.size} latest manga")
                Toast.makeText(this@ApiTestActivity, "Fetched ${mangaList.size} latest manga", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching latest manga: ${e.message}", e)
                Log.e(TAG, "Error details:", e)
                Toast.makeText(this@ApiTestActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return mangaList
    }

    private fun searchManga(query: String): List<Manga> {
        var mangaList = emptyList<Manga>()

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Searching manga with query: $query")
                val response = mangaApiService.searchManga(query)

                // Log detailed response information
                Log.d(TAG, "API Search Response: code=${response.code}, items=${response.data.size}")

                if (response.data.isNotEmpty()) {
                    val firstItem = response.data.first()
                    Log.d(TAG, "First search result: id=${firstItem.idString}, title=${firstItem.title}")
                }

                mangaList = response.data.map { it.toManga() }
                Log.d(TAG, "Found ${mangaList.size} manga matching query")
                Toast.makeText(this@ApiTestActivity, "Found ${mangaList.size} manga matching query", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error searching manga: ${e.message}", e)
                Log.e(TAG, "Error details:", e)
                Toast.makeText(this@ApiTestActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return mangaList
    }
}

@Composable
fun ApiTestScreen(
    onFetchManga: () -> List<Manga>,
    onFetchLatest: () -> List<Manga>,
    onSearchManga: () -> List<Manga>
) {
    var mangaList by remember { mutableStateOf<List<Manga>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "API Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { mangaList = onFetchManga() }) {
                Text("Fetch Manga")
            }

            Button(onClick = { mangaList = onFetchLatest() }) {
                Text("Fetch Latest")
            }

            Button(onClick = { mangaList = onSearchManga() }) {
                Text("Search Manga")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(mangaList) { manga ->
                MangaItem(manga = manga)
            }
        }
    }
}

@Composable
fun MangaItem(manga: Manga) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = manga.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (manga.subTitle.isNotEmpty()) {
                Text(
                    text = manga.subTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ID: ${manga.id}",
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Status: ${manga.status}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Authors: ${if (manga.authors.isEmpty()) "Unknown" else manga.authors.joinToString(", ")}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Genres: ${manga.genres.joinToString(", ")}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Chapters: ${manga.chapters}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Type: ${manga.type}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (manga.nsfw) {
                Text(
                    text = "NSFW Content",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Created: ${manga.createdAt}",
                fontSize = 12.sp
            )

            Text(
                text = "Updated: ${manga.updatedAt}",
                fontSize = 12.sp
            )
        }
    }
}
