package com.example.androiddevelopmenttask.ui.test

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.androiddevelopmenttask.data.api.MangaApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MangaVerseApiTestActivity : ComponentActivity() {

    @Inject
    lateinit var mangaApiService: MangaApiService

    private val TAG = "MangaVerseApiTest"

    // UI state
    private val _testResult = mutableStateOf<String?>(null)
    private val _isError = mutableStateOf(false)
    private val _isLoading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MangaVerseApiTestScreen(
                    onTestConnection = { testApiConnection() },
                    testResult = _testResult.value,
                    isError = _isError.value,
                    isLoading = _isLoading.value
                )
            }
        }
    }

    private fun testApiConnection() {
        _isLoading.value = true
        _isError.value = false
        _testResult.value = "Testing API connection..."

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Testing API connection...")

                // First, check network connectivity
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val isConnected = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                } else {
                    @Suppress("DEPRECATION")
                    connectivityManager.activeNetworkInfo?.isConnected == true
                }

                Log.d(TAG, "Network connected: $isConnected")
                showToast("Network connected: $isConnected")

                // Then, check if we can resolve the host
                try {
                    val host = "mangaverse-api.p.rapidapi.com"

                    // Try multiple DNS resolution approaches
                    try {
                        // Try system DNS first
                        val inetAddress = java.net.InetAddress.getByName(host)
                        Log.d(TAG, "Successfully resolved host $host to ${inetAddress.hostAddress}")
                        showToast("Host resolution successful: $host -> ${inetAddress.hostAddress}")
                    } catch (e: Exception) {
                        Log.e(TAG, "System DNS resolution failed: ${e.message}", e)

                        // Try hardcoded IP as fallback
                        try {
                            val hardcodedIp = "104.18.6.80" // IP for mangaverse-api.p.rapidapi.com
                            val addr = java.net.InetAddress.getByAddress(
                                host,
                                hardcodedIp.split(".").map { it.toInt().toByte() }.toByteArray()
                            )
                            Log.d(TAG, "Using hardcoded IP for $host: $hardcodedIp")
                            showToast("Host resolution successful (using hardcoded IP): $host -> $hardcodedIp")
                        } catch (e2: Exception) {
                            Log.e(TAG, "Hardcoded IP resolution failed: ${e2.message}", e2)
                            showToast("Host resolution failed: ${e.message}", true)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot resolve host: ${e.message}", e)
                    showToast("Host resolution failed: ${e.message}", true)
                    // Continue anyway to see what happens with the API request
                }

                // Try to make an API request
                try {
                    Log.d(TAG, "Making API request to getMangaList...")
                    val response = mangaApiService.getMangaList(page = 1, limit = 10)

                    // Log detailed response information
                    Log.d(TAG, "API Response: code=${response.code}, items=${response.data.size}")

                    if (response.data.isNotEmpty()) {
                        val firstItem = response.data.first()
                        Log.d(TAG, "First item: id=${firstItem.idString}, title=${firstItem.title}")

                        // Check if we're using mock data - the mock data always has "One Piece" as first manga
                        // Real API data has different manga titles like "A World of Gold to You"
                        val isMockData = firstItem.title.contains("One Piece", ignoreCase = true)

                        if (isMockData) {
                            Log.d(TAG, "Using mock data (detected by first item: ${firstItem.title})")
                            showToast("Success! Received ${response.data.size} manga items.\nFirst manga: ${firstItem.title}\n(Using mock data)")
                        } else {
                            Log.d(TAG, "Using real API data (detected by first item: ${firstItem.title})")
                            showToast("Success! Received ${response.data.size} manga items.\nFirst manga: ${firstItem.title}\n(Using real API data)")
                        }
                    } else {
                        Log.d(TAG, "API returned empty data list")
                        showToast("API returned empty data list", true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error making API request: ${e.message}", e)
                    Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                    Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                    showToast("API request failed: ${e.message}\nError type: ${e.javaClass.simpleName}", true)

                    // Try a direct HTTP request using OkHttp
                    try {
                        Log.d(TAG, "Trying direct HTTP request...")

                        withContext(Dispatchers.IO) {
                            // Create a custom DNS resolver
                            val dns = object : okhttp3.Dns {
                                override fun lookup(hostname: String): List<java.net.InetAddress> {
                                    Log.d(TAG, "Direct HTTP request: Looking up hostname: $hostname")

                                    // Try system DNS first
                                    try {
                                        val addresses = okhttp3.Dns.SYSTEM.lookup(hostname)
                                        Log.d(TAG, "Direct HTTP request: System DNS resolved $hostname to $addresses")
                                        return addresses
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Direct HTTP request: System DNS lookup failed: ${e.message}")
                                    }

                                    // If system DNS fails, try hardcoded IP for the specific host
                                    if (hostname == "mangaverse-api.p.rapidapi.com") {
                                        val hardcodedIp = "104.18.6.80" // IP for mangaverse-api.p.rapidapi.com
                                        val addr = java.net.InetAddress.getByAddress(
                                            hostname,
                                            hardcodedIp.split(".").map { it.toInt().toByte() }.toByteArray()
                                        )
                                        Log.d(TAG, "Direct HTTP request: Using hardcoded IP for $hostname: $hardcodedIp")
                                        return listOf(addr)
                                    }

                                    throw java.net.UnknownHostException("Failed to resolve host: $hostname")
                                }
                            }

                            val client = OkHttpClient.Builder()
                                .dns(dns)
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .build()

                            val request = Request.Builder()
                                .url("https://mangaverse-api.p.rapidapi.com/manga/fetch?page=1&limit=10")
                                .addHeader("X-RapidAPI-Key", "14a88debeamsh00fec5566b32637p11d1e0jsn52bd5dc0db14")
                                .addHeader("X-RapidAPI-Host", "mangaverse-api.p.rapidapi.com")
                                .build()

                            Log.d(TAG, "Executing direct HTTP request...")

                            try {
                                val response = client.newCall(request).execute()

                                if (response.isSuccessful) {
                                    val responseBody = response.body?.string() ?: "Empty response"
                                    Log.d(TAG, "Direct HTTP request successful: $responseBody")

                                    withContext(Dispatchers.Main) {
                                        showToast("Direct HTTP request successful!")
                                    }
                                } else {
                                    Log.e(TAG, "Direct HTTP request failed: ${response.code}")
                                    val errorBody = response.body?.string() ?: "No error body"
                                    Log.e(TAG, "Error response body: $errorBody")

                                    withContext(Dispatchers.Main) {
                                        showToast("Direct HTTP request failed: ${response.code}", true)
                                    }
                                }
                            } catch (e3: Exception) {
                                Log.e(TAG, "Exception during HTTP execution: ${e3.message}", e3)

                                withContext(Dispatchers.Main) {
                                    showToast("HTTP execution error: ${e3.message}", true)
                                }
                            }
                        }
                    } catch (e2: Exception) {
                        Log.e(TAG, "Direct HTTP request error: ${e2.message}", e2)
                        showToast("Direct HTTP request error: ${e2.message}", true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
                showToast("Error: ${e.message}", true)
            }
        }
    }

    private fun showToast(message: String, isError: Boolean = false) {
        _isLoading.value = false
        _isError.value = isError
        _testResult.value = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun MangaVerseApiTestScreen(
    onTestConnection: () -> Unit,
    testResult: String?,
    isError: Boolean,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MangaVerse API Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { onTestConnection() },
            modifier = Modifier.padding(bottom = 32.dp),
            enabled = !isLoading
        ) {
            Text("Test API Connection")
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        testResult?.let {
            Text(
                text = it,
                color = if (isError) Color.Red else Color.Blue,
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        }

        // Add some helpful information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Troubleshooting Tips:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("1. Make sure you have an active internet connection")
                Text("2. Check if the RapidAPI key is valid")
                Text("3. Try using a different network (WiFi vs. Mobile data)")
                Text("4. Check if the API endpoint is correct")
                Text("5. Look at the Android Studio Logcat for detailed error messages")
            }
        }
    }
}
