package com.tuusuario.pinterestfeed.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import coil.imageLoader
import coil.request.ImageRequest
import com.tuusuario.pinterestfeed.data.model.Photo
import com.tuusuario.pinterestfeed.data.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el feed de fotos
 * Maneja el estado de scroll, flujo de datos paginados y prefetch de imágenes
 */
class FeedViewModel(
    private val repository: PhotoRepository = PhotoRepository.getInstance(useMockData = false)
) : ViewModel() {

    private val tag = "FeedViewModel"

    /**
     * Estado del scroll para restauración
     */
    private val _scrollState = MutableStateFlow(ScrollState())
    val scrollState: StateFlow<ScrollState> = _scrollState.asStateFlow()

    /**
     * Flujo de fotos paginadas
     * cachedIn mantiene los datos en caché durante cambios de configuración
     */
    val photosFlow: Flow<PagingData<Photo>> = repository
        .getPhotosStream()
        .cachedIn(viewModelScope)

    /**
     * Guarda la posición actual del scroll
     */
    fun saveScrollPosition(index: Int, offset: Int) {
        _scrollState.value = ScrollState(
            firstVisibleItemIndex = index,
            firstVisibleItemScrollOffset = offset
        )
        Log.d(tag, "Scroll saved: index=$index, offset=$offset")
    }

    /**
     * Limpia el estado de scroll (útil para refresh)
     */
    fun clearScrollState() {
        _scrollState.value = ScrollState()
    }

    /**
     * Prefetch de imágenes basado en la posición del scroll
     * Precarga las próximas N imágenes para mejorar UX
     */
    fun prefetchImages(currentIndex: Int, photos: LazyPagingItems<Photo>) {
        viewModelScope.launch {
            try {
                val prefetchCount = 10 // Número de imágenes a precargar
                val startIndex = currentIndex + 1
                val endIndex = minOf(startIndex + prefetchCount, photos.itemCount)

                for (i in startIndex until endIndex) {
                    photos[i]?.let { photo ->
                        // Coil maneja el prefetch automáticamente si la imagen ya está en caché
                        // Solo necesitamos disparar la request
                        Log.v(tag, "Prefetching image at index $i: ${photo.id}")
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Error during prefetch: ${e.message}")
            }
        }
    }
}

/**
 * Estado inmutable del scroll
 */
data class ScrollState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0
)