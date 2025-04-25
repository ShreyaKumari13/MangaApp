package com.example.androiddevelopmenttask

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androiddevelopmenttask.data.api.MangaApiService
import com.example.androiddevelopmenttask.presentation.common.theme.AndroidDevelopmentTaskTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApiTestActivity : ComponentActivity() {

    @Inject
    lateinit var mangaApiService: MangaApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidDevelopmentTaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiTestScreen(mangaApiService)
                }
            }
        }
    }
}

@Composable
fun ApiTestScreen(apiService: MangaApiService) {
    val TAG = "ApiTestScreen"
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("Press the button to test the API") }
    var success by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MangaVerse API Test",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                success = false
                resultText = "Testing API..."

                coroutineScope.launch {
                    try {
                        Log.d(TAG, "Making API request...")

                        // Check if we're using the mock service
                        val isMockService = apiService is com.example.androiddevelopmenttask.data.api.MockMangaApiService
                        Log.d(TAG, "Using mock service: $isMockService")

                        val response = apiService.getMangaList(1, 10)
                        Log.d(TAG, "API Response: $response")

                        val mangaCount = response.data.size
                        resultText = if (mangaCount > 0) {
                            success = true
                            "Success! Received $mangaCount manga items.\n\n" +
                            "First manga: ${response.data.firstOrNull()?.title ?: "None"}\n\n" +
                            if (isMockService) "(Using mock data)" else "(Using real API data)"
                        } else {
                            "API returned success but no manga items were found."
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "API Error", e)
                        resultText = "Error: ${e.message}\n\n" +
                                     "Please check your internet connection and make sure the API is accessible."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Test API Connection")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
