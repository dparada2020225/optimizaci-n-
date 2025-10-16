package com.tuusuario.pinterestfeed.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modelo de foto con dimensiones variables para el feed tipo Pinterest
 */
@Parcelize
data class Photo(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val title: String,
    val author: String = "Unknown"
) : Parcelable {

    /**
     * Calcula el aspect ratio para mantener proporciones
     */
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()

    /**
     * URL optimizada con tamaño específico para reducir uso de red
     */
    fun getOptimizedUrl(targetWidth: Int): String {
        return "$url/$targetWidth/${(targetWidth / aspectRatio).toInt()}"
    }
}

/**
 * Response de la API simulada
 */
data class PhotoListResponse(
    val photos: List<Photo>,
    val page: Int,
    val hasMore: Boolean
)