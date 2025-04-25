package com.example.androiddevelopmenttask.presentation.common.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object MangaDetail : Screen("manga_detail/{mangaId}") {
        fun createRoute(mangaId: Int) = "manga_detail/$mangaId"
    }
    object FaceDetection : Screen("face_detection")
}
