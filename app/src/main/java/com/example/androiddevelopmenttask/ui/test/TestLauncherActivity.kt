package com.example.androiddevelopmenttask.ui.test

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevelopmenttask.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestLauncherActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TestLauncherScreen(
                    onLaunchMainApp = {
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                    onLaunchApiTest = {
                        startActivity(Intent(this, com.example.androiddevelopmenttask.ApiTestActivity::class.java))
                    },
                    onLaunchMangaVerseApiTest = {
                        startActivity(Intent(this, MangaVerseApiTestActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun TestLauncherScreen(
    onLaunchMainApp: () -> Unit,
    onLaunchApiTest: () -> Unit,
    onLaunchMangaVerseApiTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Test Launcher",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLaunchMainApp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Launch Main App")
        }

        Button(
            onClick = onLaunchApiTest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Launch API Test")
        }

        Button(
            onClick = onLaunchMangaVerseApiTest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Launch MangaVerse API Test")
        }
    }
}
