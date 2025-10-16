package com.tuusuario.pinterestfeed.ui.screens.feed

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.tuusuario.pinterestfeed.data.model.Photo
import com.tuusuario.pinterestfeed.ui.components.*
import kotlinx.coroutines.launch

/**
 * Pantalla principal del feed tipo Pinterest
 * Grid staggered con scroll infinito y estados de carga
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPhotoClick: (Photo) -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val photos = viewModel.photosFlow.collectAsLazyPagingItems()
    val scrollState = viewModel.scrollState.collectAsState()
    val staggeredGridState = rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = scrollState.value.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = scrollState.value.firstVisibleItemScrollOffset
    )
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { staggeredGridState.firstVisibleItemIndex > 5 }
    }

    // Guardar posición del scroll
    LaunchedEffect(staggeredGridState) {
        snapshotFlow {
            Pair(
                staggeredGridState.firstVisibleItemIndex,
                staggeredGridState.firstVisibleItemScrollOffset
            )
        }.collect { (index, offset) ->
            viewModel.saveScrollPosition(index, offset)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pinterest Feed") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            staggeredGridState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(Icons.Default.ArrowUpward, "Scroll to top")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                photos.loadState.refresh is LoadState.Loading && photos.itemCount == 0 -> {
                    InitialLoadingState()
                }
                photos.loadState.refresh is LoadState.Error && photos.itemCount == 0 -> {
                    ErrorRetryItem(
                        message = "Failed to load photos. Check your connection.",
                        onRetry = { photos.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                photos.itemCount == 0 -> {
                    EmptyStateItem()
                }
                else -> {
                    PhotoGrid(
                        photos = photos,
                        state = staggeredGridState,
                        onPhotoClick = onPhotoClick
                    )
                }
            }
        }
    }
}

/**
 * Grid staggered optimizado con keys estables
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGrid(
    photos: LazyPagingItems<Photo>,
    state: LazyStaggeredGridState,
    onPhotoClick: (Photo) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = state,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = photos.itemCount,
            key = { index -> photos[index]?.id ?: "item_$index" }
        ) { index ->
            photos[index]?.let { photo ->
                PhotoItem(
                    photo = photo,
                    onClick = { onPhotoClick(photo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                )
            }
        }

        // Estados de paginación
        when {
            photos.loadState.append is LoadState.Loading -> {
                item(span = StaggeredGridItemSpan.FullLine) {
                    LoadingItem()
                }
            }
            photos.loadState.append is LoadState.Error -> {
                item(span = StaggeredGridItemSpan.FullLine) {
                    ErrorRetryItem(
                        message = "Failed to load more photos",
                        onRetry = { photos.retry() }
                    )
                }
            }
        }
    }
}

/**
 * Estado de carga inicial con skeletons
 */
@Composable
private fun InitialLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Text(
                text = "Loading photos...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}