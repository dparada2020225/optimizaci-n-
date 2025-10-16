package com.tuusuario.pinterestfeed.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.tuusuario.pinterestfeed.data.model.Photo
import com.tuusuario.pinterestfeed.data.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para el feed de fotos
 * Maneja el estado de scroll y el flujo de datos paginados
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
}

/**
 * Estado inmutable del scroll
 */
data class ScrollState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0
)