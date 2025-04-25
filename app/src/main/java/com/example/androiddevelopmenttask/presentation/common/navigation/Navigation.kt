package com.example.androiddevelopmenttask.presentation.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.androiddevelopmenttask.presentation.auth.LoginScreen
import com.example.androiddevelopmenttask.presentation.auth.RegisterScreen
import com.example.androiddevelopmenttask.presentation.face.FaceDetectionScreen
import com.example.androiddevelopmenttask.presentation.home.HomeScreen
import com.example.androiddevelopmenttask.presentation.manga.MangaDetailScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.MangaDetail.route,
            arguments = listOf(
                navArgument("mangaId") {
                    type = NavType.IntType
                }
            )
        ) {
            MangaDetailScreen(navController = navController)
        }

        composable(Screen.FaceDetection.route) {
            FaceDetectionScreen(navController = navController)
        }
    }
}
