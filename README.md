# Zenithtra - Manga App

This Android application follows Clean Architecture + MVVM, using Jetpack Compose for UI and Single Activity Architecture with Jetpack Navigation Component.

## Features
- User Authentication with Room DB
- Manga data fetching & caching
- Face recognition using MediaPipe
- Bottom Navigation with two items (Manga Screen and Face Recognition Screen)

## API Integration
This app uses the MangaVerse API from RapidAPI to fetch real manga data. To use the API:

1. Sign up for a free account on [RapidAPI](https://rapidapi.com/)
2. Subscribe to the [MangaVerse API](https://rapidapi.com/sagarotite/api/mangaverse-api)
3. Get your API key from the RapidAPI dashboard
4. Open `NetworkModule.kt` and replace `YOUR_RAPIDAPI_KEY` with your actual API key:

```kotlin
private const val RAPID_API_KEY = "YOUR_RAPIDAPI_KEY"
```

## API Endpoints
The app uses the following endpoints from the MangaVerse API:

- `/fetch-manga` - Get a list of manga with pagination
- `/fetch-latest` - Get latest manga
- `/search-manga` - Search for manga by title, author, or genre
- `/get-manga` - Get manga details by ID
- `/fetch-chapters` - Get chapters for a manga
- `/fetch-images` - Get images for a chapter

## Offline Support
The app implements a caching mechanism using Room database to provide offline access to previously fetched manga data.

## Architecture
- **UI Layer**: Jetpack Compose
- **Presentation Layer**: ViewModels with StateFlow
- **Domain Layer**: Use cases and repository interfaces
- **Data Layer**: Repository implementations, Room database, and API service

## Libraries Used
- Jetpack Compose for UI
- Hilt for dependency injection
- Room for local database
- Retrofit for network requests
- Coil for image loading
- MediaPipe for face detection
- Navigation Component for navigation
- DataStore for preferences
