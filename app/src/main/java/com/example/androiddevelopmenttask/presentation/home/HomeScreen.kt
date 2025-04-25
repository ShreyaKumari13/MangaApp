package com.example.androiddevelopmenttask.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.androiddevelopmenttask.presentation.common.components.BottomNavigationBar
import com.example.androiddevelopmenttask.presentation.common.navigation.BottomNavItem
import com.example.androiddevelopmenttask.presentation.common.navigation.Screen
import com.example.androiddevelopmenttask.presentation.home.components.MangaCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val mangaListState by viewModel.mangaListState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                val viewportSize = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

                (lastVisibleItem.index + 1 >= layoutInfo.totalItemsCount &&
                        lastVisibleItem.offset.y + lastVisibleItem.size.height <= viewportSize)
            }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && mangaListState is MangaListState.Success) {
            viewModel.loadMangaList()
        }
    }

    LaunchedEffect(mangaListState) {
        if (mangaListState is MangaListState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    (mangaListState as MangaListState.Error).message
                )
            }
        }
    }

    val isRefreshing = mangaListState is MangaListState.Loading
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = listOf(
                    BottomNavItem.Manga,
                    BottomNavItem.Face
                )
            )
        }
    ) { paddingValues ->
        // Note: SwipeRefresh is deprecated but we're keeping it for now
        // A future update should migrate to Modifier.pullRefresh() when available
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshMangaList() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zenithtra",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        // Debug button to verify API source and run diagnostics
                        Button(
                            onClick = {
                                // Log diagnostic information
                                viewModel.logDiagnosticInfo()

                                // Launch API test activity
                                val intent = android.content.Intent(
                                    navController.context,
                                    com.example.androiddevelopmenttask.ApiTestActivity::class.java
                                )
                                navController.context.startActivity(intent)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Test API")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (mangaListState) {
                        is MangaListState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is MangaListState.Success, is MangaListState.LoadingMore -> {
                            val mangaList = if (mangaListState is MangaListState.Success) {
                                (mangaListState as MangaListState.Success).mangaList
                            } else {
                                (mangaListState as MangaListState.LoadingMore).mangaList
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                state = gridState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(mangaList) { manga ->
                                    MangaCard(
                                        manga = manga,
                                        onClick = {
                                            navController.navigate(
                                                Screen.MangaDetail.createRoute(manga.id)
                                            )
                                        }
                                    )
                                }
                            }

                            if (mangaListState is MangaListState.LoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is MangaListState.Error -> {
                            val errorState = mangaListState as MangaListState.Error
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = errorState.message,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { viewModel.refreshMangaList() }
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
