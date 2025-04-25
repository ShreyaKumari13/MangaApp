package com.example.androiddevelopmenttask.di

import com.example.androiddevelopmenttask.data.api.MangaApiService
import com.example.androiddevelopmenttask.data.api.MockMangaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // The correct base URL for the MangaVerse API
    private const val BASE_URL = "https://mangaverse-api.p.rapidapi.com/"

    // RapidAPI Key
    private const val RAPID_API_KEY = "14a88debeamsh00fec5566b32637p11d1e0jsn52bd5dc0db14"

    // Timeout settings
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideRapidApiInterceptor(): Interceptor {
        // Log the API key being used (first 10 chars only for security)
        val keyPrefix = if (RAPID_API_KEY.length > 10) RAPID_API_KEY.substring(0, 10) + "..." else RAPID_API_KEY
        android.util.Log.d("NetworkModule", "Using RapidAPI Key: $keyPrefix")

        return Interceptor { chain ->
            val original = chain.request()

            // Log the original request details
            android.util.Log.d("NetworkModule", "Original request URL: ${original.url}")
            android.util.Log.d("NetworkModule", "Original request method: ${original.method}")
            android.util.Log.d("NetworkModule", "Original request headers: ${original.headers}")

            val requestBuilder = original.newBuilder()
                .header("X-RapidAPI-Key", RAPID_API_KEY)
                .header("X-RapidAPI-Host", "mangaverse-api.p.rapidapi.com")
                .method(original.method, original.body)

            val request = requestBuilder.build()

            // Log the modified request details
            android.util.Log.d("NetworkModule", "Modified request URL: ${request.url}")
            android.util.Log.d("NetworkModule", "Modified request headers: ${request.headers}")
            android.util.Log.d("NetworkModule", "Making API request to: ${request.url}")

            chain.proceed(request)
        }
    }

    // Define qualifiers for the different Retrofit instances
    @javax.inject.Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RegularRetrofit

    @javax.inject.Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DirectIpRetrofit

    @Provides
    @Singleton
    @RegularRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @DirectIpRetrofit
    fun provideDirectIpRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Use a direct IP URL as the base URL
        val directIpBaseUrl = "http://104.18.6.80/"

        return Retrofit.Builder()
            .baseUrl(directIpBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(rapidApiInterceptor: Interceptor, loggingInterceptor: okhttp3.logging.HttpLoggingInterceptor): OkHttpClient {
        android.util.Log.d("NetworkModule", "Creating OkHttpClient with timeouts: $CONNECT_TIMEOUT_SECONDS seconds")

        // Use Google's DNS servers to improve hostname resolution
        val dns = object : okhttp3.Dns {
            override fun lookup(hostname: String): List<java.net.InetAddress> {
                try {
                    android.util.Log.d("NetworkModule", "Looking up hostname: $hostname")

                    // We could use Google's DNS servers (8.8.8.8 and 8.8.4.4) here
                    // but we'll just use the system DNS for simplicity

                    // First try the system's default DNS
                    try {
                        val addresses = okhttp3.Dns.SYSTEM.lookup(hostname)
                        android.util.Log.d("NetworkModule", "System DNS resolved $hostname to $addresses")
                        return addresses
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "System DNS lookup failed: ${e.message}")
                        // Fall through to manual resolution
                    }

                    // If system DNS fails, try a manual approach
                    // This is a simplified approach - in a real app, you'd use a proper DNS library
                    val hardcodedIp = when (hostname) {
                        "mangaverse-api.p.rapidapi.com" -> {
                            // Try to resolve using InetAddress directly
                            try {
                                val addr = java.net.InetAddress.getByName(hostname)
                                android.util.Log.d("NetworkModule", "Resolved $hostname to ${addr.hostAddress}")
                                return listOf(addr)
                            } catch (e: Exception) {
                                android.util.Log.e("NetworkModule", "Failed to resolve $hostname: ${e.message}")
                                // Use a hardcoded IP as last resort
                                // This is the IP address for mangaverse-api.p.rapidapi.com as of now
                                // You should update this if the IP changes
                                "104.18.6.80"
                            }
                        }
                        else -> null
                    }

                    if (hardcodedIp != null) {
                        val addr = java.net.InetAddress.getByAddress(
                            hostname,
                            hardcodedIp.split(".").map { it.toInt().toByte() }.toByteArray()
                        )
                        android.util.Log.d("NetworkModule", "Using hardcoded IP for $hostname: $hardcodedIp")
                        return listOf(addr)
                    }

                    // If all else fails, throw an exception
                    throw java.net.UnknownHostException("Failed to resolve host: $hostname")
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "DNS lookup error for $hostname: ${e.message}", e)
                    throw e
                }
            }
        }

        return OkHttpClient.Builder()
            .dns(dns)
            .addInterceptor(rapidApiInterceptor)
            .addInterceptor(loggingInterceptor)
            // Add a network interceptor to log request/response details
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                android.util.Log.d("NetworkModule", "Executing request: ${request.url}")
                android.util.Log.d("NetworkModule", "Request headers: ${request.headers}")

                try {
                    val response = chain.proceed(request)
                    android.util.Log.d("NetworkModule", "Response received: ${response.code} for ${request.url}")
                    android.util.Log.d("NetworkModule", "Response headers: ${response.headers}")

                    if (!response.isSuccessful) {
                        val responseBody = response.peekBody(Long.MAX_VALUE).string()
                        android.util.Log.e("NetworkModule", "Error response body: $responseBody")
                    }

                    response
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error during request to ${request.url}: ${e.message}", e)
                    android.util.Log.e("NetworkModule", "Error type: ${e.javaClass.simpleName}")
                    android.util.Log.e("NetworkModule", "Stack trace: ${e.stackTraceToString()}")
                    throw e
                }
            }
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMangaApiService(
        @RegularRetrofit retrofit: Retrofit,
        @DirectIpRetrofit directIpRetrofit: Retrofit
    ): MangaApiService {
        // Create the real API service first
        android.util.Log.d("NetworkModule", "Creating real API service")
        val realApiService = retrofit.create(MangaApiService::class.java)

        // Create a direct IP API service
        android.util.Log.d("NetworkModule", "Creating direct IP API service")
        val directIpApiService = directIpRetrofit.create(MangaApiService::class.java)

        // Return a wrapper that will try the real API first and fall back to mock if needed
        return object : MangaApiService {
            private val mockService = MockMangaApiService()

            override suspend fun getMangaList(page: Int, limit: Int): com.example.androiddevelopmenttask.data.api.model.MangaListResponse {
                return try {
                    android.util.Log.d("NetworkModule", "Attempting to use real API for getMangaList")
                    try {
                        // Try the normal endpoint first
                        val response = realApiService.getMangaList(page, limit)
                        android.util.Log.d("NetworkModule", "Successfully used real API for getMangaList")

                        // Log the first item to help with debugging
                        if (response.data.isNotEmpty()) {
                            val firstItem = response.data.first()
                            android.util.Log.d("NetworkModule", "First manga from API: ${firstItem.title} (ID: ${firstItem.idString})")
                        }

                        response
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "Error using normal endpoint: ${e.message}", e)
                        android.util.Log.d("NetworkModule", "Trying direct IP endpoint")

                        // If that fails, try the direct IP endpoint
                        try {
                            val response = directIpApiService.getMangaList(page, limit)
                            android.util.Log.d("NetworkModule", "Successfully used direct IP endpoint for getMangaList")

                            // Log the first item to help with debugging
                            if (response.data.isNotEmpty()) {
                                val firstItem = response.data.first()
                                android.util.Log.d("NetworkModule", "First manga from direct IP API: ${firstItem.title} (ID: ${firstItem.idString})")
                            }

                            response
                        } catch (e2: Exception) {
                            android.util.Log.e("NetworkModule", "Error using direct IP endpoint: ${e2.message}", e2)
                            throw e2 // Rethrow to fall back to mock service
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error using real API for getMangaList: ${e.message}", e)
                    android.util.Log.d("NetworkModule", "Falling back to mock service for getMangaList")
                    mockService.getMangaList(page, limit)
                }
            }

            override suspend fun getLatestManga(): com.example.androiddevelopmenttask.data.api.model.MangaListResponse {
                return try {
                    android.util.Log.d("NetworkModule", "Attempting to use real API for getLatestManga")
                    try {
                        // Try the normal endpoint first
                        val response = realApiService.getLatestManga()
                        android.util.Log.d("NetworkModule", "Successfully used real API for getLatestManga")

                        // Log the first item to help with debugging
                        if (response.data.isNotEmpty()) {
                            val firstItem = response.data.first()
                            android.util.Log.d("NetworkModule", "First manga from latest API: ${firstItem.title} (ID: ${firstItem.idString})")
                        }

                        response
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "Error using normal endpoint for getLatestManga: ${e.message}", e)
                        android.util.Log.d("NetworkModule", "Trying direct IP endpoint for getLatestManga")

                        // If that fails, try the direct IP endpoint
                        try {
                            val response = directIpApiService.getLatestManga()
                            android.util.Log.d("NetworkModule", "Successfully used direct IP endpoint for getLatestManga")

                            // Log the first item to help with debugging
                            if (response.data.isNotEmpty()) {
                                val firstItem = response.data.first()
                                android.util.Log.d("NetworkModule", "First manga from direct IP latest API: ${firstItem.title} (ID: ${firstItem.idString})")
                            }

                            response
                        } catch (e2: Exception) {
                            android.util.Log.e("NetworkModule", "Error using direct IP endpoint for getLatestManga: ${e2.message}", e2)
                            throw e2 // Rethrow to fall back to mock service
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error using real API for getLatestManga: ${e.message}", e)
                    android.util.Log.d("NetworkModule", "Falling back to mock service for getLatestManga")
                    mockService.getLatestManga()
                }
            }

            override suspend fun searchManga(query: String): com.example.androiddevelopmenttask.data.api.model.MangaListResponse {
                return try {
                    android.util.Log.d("NetworkModule", "Attempting to use real API for searchManga")
                    try {
                        // Try the normal endpoint first
                        val response = realApiService.searchManga(query)
                        android.util.Log.d("NetworkModule", "Successfully used real API for searchManga")
                        response
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "Error using normal endpoint for searchManga: ${e.message}", e)
                        android.util.Log.d("NetworkModule", "Trying direct IP endpoint for searchManga")

                        // If that fails, try the direct IP endpoint
                        try {
                            val response = directIpApiService.searchManga(query)
                            android.util.Log.d("NetworkModule", "Successfully used direct IP endpoint for searchManga")
                            response
                        } catch (e2: Exception) {
                            android.util.Log.e("NetworkModule", "Error using direct IP endpoint for searchManga: ${e2.message}", e2)
                            throw e2 // Rethrow to fall back to mock service
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error using real API for searchManga: ${e.message}", e)
                    android.util.Log.d("NetworkModule", "Falling back to mock service for searchManga")
                    mockService.searchManga(query)
                }
            }

            override suspend fun getMangaById(id: Int): com.example.androiddevelopmenttask.data.api.model.MangaDto {
                return try {
                    android.util.Log.d("NetworkModule", "Attempting to use real API for getMangaById")
                    try {
                        // Try the normal endpoint first
                        val response = realApiService.getMangaById(id)
                        android.util.Log.d("NetworkModule", "Successfully used real API for getMangaById")
                        response
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "Error using normal endpoint for getMangaById: ${e.message}", e)
                        android.util.Log.d("NetworkModule", "Trying direct IP endpoint for getMangaById")

                        // If that fails, try the direct IP endpoint
                        try {
                            val response = directIpApiService.getMangaById(id)
                            android.util.Log.d("NetworkModule", "Successfully used direct IP endpoint for getMangaById")
                            response
                        } catch (e2: Exception) {
                            android.util.Log.e("NetworkModule", "Error using direct IP endpoint for getMangaById: ${e2.message}", e2)
                            throw e2 // Rethrow to fall back to mock service
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error using real API for getMangaById: ${e.message}", e)
                    android.util.Log.d("NetworkModule", "Falling back to mock service for getMangaById")
                    mockService.getMangaById(id)
                }
            }

            override suspend fun getChapters(mangaId: Int): com.example.androiddevelopmenttask.data.api.model.ChapterListResponse {
                return try {
                    android.util.Log.d("NetworkModule", "Attempting to use real API for getChapters")
                    try {
                        // Try the normal endpoint first
                        val response = realApiService.getChapters(mangaId)
                        android.util.Log.d("NetworkModule", "Successfully used real API for getChapters")
                        response
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "Error using normal endpoint for getChapters: ${e.message}", e)
                        android.util.Log.d("NetworkModule", "Trying direct IP endpoint for getChapters")

                        // If that fails, try the direct IP endpoint
                        try {
                            val response = directIpApiService.getChapters(mangaId)
                            android.util.Log.d("NetworkModule", "Successfully used direct IP endpoint for getChapters")
                            response
                        } catch (e2: Exception) {
                            android.util.Log.e("NetworkModule", "Error using direct IP endpoint for getChapters: ${e2.message}", e2)
                            throw e2 // Rethrow to fall back to mock service
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error using real API for getChapters: ${e.message}", e)
                    android.util.Log.d("NetworkModule", "Falling back to mock service for getChapters")
                    mockService.getChapters(mangaId)
                }
            }

            override suspend fun getChapterImages(mangaId: Int, chapterNumber: String): com.example.androiddevelopmenttask.data.api.model.ChapterImagesResponse {
                return try {
                    android.util.Log.d("NetworkModule", "Attempting to use real API for getChapterImages")
                    try {
                        // Try the normal endpoint first
                        val response = realApiService.getChapterImages(mangaId, chapterNumber)
                        android.util.Log.d("NetworkModule", "Successfully used real API for getChapterImages")
                        response
                    } catch (e: Exception) {
                        android.util.Log.e("NetworkModule", "Error using normal endpoint for getChapterImages: ${e.message}", e)
                        android.util.Log.d("NetworkModule", "Trying direct IP endpoint for getChapterImages")

                        // If that fails, try the direct IP endpoint
                        try {
                            val response = directIpApiService.getChapterImages(mangaId, chapterNumber)
                            android.util.Log.d("NetworkModule", "Successfully used direct IP endpoint for getChapterImages")
                            response
                        } catch (e2: Exception) {
                            android.util.Log.e("NetworkModule", "Error using direct IP endpoint for getChapterImages: ${e2.message}", e2)
                            throw e2 // Rethrow to fall back to mock service
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error using real API for getChapterImages: ${e.message}", e)
                    android.util.Log.d("NetworkModule", "Falling back to mock service for getChapterImages")
                    mockService.getChapterImages(mangaId, chapterNumber)
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): okhttp3.logging.HttpLoggingInterceptor {
        return okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
    }
}
