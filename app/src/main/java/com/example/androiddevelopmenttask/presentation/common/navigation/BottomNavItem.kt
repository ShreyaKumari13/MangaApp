package com.example.androiddevelopmenttask.presentation.common.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Manga : BottomNavItem(
        route = Screen.Home.route,
        title = "Manga",
        icon = Icons.Default.Book
    )

    object Face : BottomNavItem(
        route = Screen.FaceDetection.route,
        title = "Face",
        icon = Icons.Default.Face
    )
}
