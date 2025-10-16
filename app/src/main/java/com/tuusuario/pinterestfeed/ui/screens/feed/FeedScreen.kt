package com.tuusuario.pinterestfeed.ui.screens.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.tuusuario.pinterestfeed.data.model.Photo
import com.tuusuario.pinterestfeed.ui.components.EmptyStateItem
import com.tuusuario.pinterestfeed.ui.components.ErrorRetryItem
import com.tuusuario.pinterestfeed.ui.components.LoadingItem
import com.tuusuario.pinterestfeed.ui.components.PhotoItem
import kotlinx.coroutines.launch

/**
 * Pantalla principal del feed tipo Pinterest con grid staggered.
 * Sin animación de ítems para máxima compatibilidad.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPhotoClick: (Photo) -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val photos = viewModel.photosFlow.collectAsLazyPagingItems()
    val scrollState = viewModel.scrollState.collectAsState()
    val staggeredGridState = androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = scrollState.value.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = scrollState.value.firstVisibleItemScrollOffset
    )
    val scope = rememberCoroutineScope()
    val showScrollToTop by androidx.compose.runtime.remember {
        derivedStateOf { staggeredGridState.firstVisibleItemIndex > 5 }
    }

    // Guardar posición del scroll
    LaunchedEffect(staggeredGridState) {
        androidx.compose.runtime.snapshotFlow {
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
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "Scroll to top"
                    )
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
 * Grid staggered optimizado con keys estables.
 * Sin Modifier.animateItem()/animateItemPlacement() para evitar issues.
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
                    modifier = Modifier.fillMaxWidth()
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
 * Estado de carga inicial con indicador centrado.
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
            CircularProgressIndicator(modifier = Modifier)
            Text(
                text = "Loading photos...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
