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
    private const val RAPID_API_KEY = "14a85de6eamsh00fec5566b32637p1b1e0jsn52bd5dc0db14"
    private const val RAPID_API_HOST = "mangaverse-api.p.rapidapi.com"

    // Timeout settings
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideRapidApiInterceptor(): Interceptor {
        // Log the API key being used (first 10 chars only for security)
        val keyPrefix = if (RAPID_API_KEY.length > 10) RAPID_API_KEY.substring(0, 10) + "..." else RAPID_API_KEY
        android.util.Log.d("NetworkModule", "Using RapidAPI Key: $keyPrefix for host: $RAPID_API_HOST")

        return Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("X-RapidAPI-Key", RAPID_API_KEY)
                .header("X-RapidAPI-Host", RAPID_API_HOST)
                .method(original.method, original.body)

            val request = requestBuilder.build()
            android.util.Log.d("NetworkModule", "Making API request to: ${request.url}")
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(rapidApiInterceptor: Interceptor, loggingInterceptor: okhttp3.logging.HttpLoggingInterceptor): OkHttpClient {
        android.util.Log.d("NetworkModule", "Creating OkHttpClient with timeouts: $CONNECT_TIMEOUT_SECONDS seconds")

        return OkHttpClient.Builder()
            .addInterceptor(rapidApiInterceptor)
            .addInterceptor(loggingInterceptor)
            // Add a network interceptor to log request/response details
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                android.util.Log.d("NetworkModule", "Executing request: ${request.url}")

                try {
                    val response = chain.proceed(request)
                    android.util.Log.d("NetworkModule", "Response received: ${response.code} for ${request.url}")
                    response
                } catch (e: Exception) {
                    android.util.Log.e("NetworkModule", "Error during request to ${request.url}: ${e.message}", e)
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
    fun provideMangaApiService(retrofit: Retrofit): MangaApiService {
        // For testing, you can use the mock service if the API key is not working
        // return MockMangaApiService()

        // Use the real API service with the valid API key
        return retrofit.create(MangaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): okhttp3.logging.HttpLoggingInterceptor {
        return okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
    }
}
