package com.tuusuario.pinterestfeed.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tuusuario.pinterestfeed.data.model.Photo
import com.tuusuario.pinterestfeed.data.remote.PhotoApi
import com.tuusuario.pinterestfeed.data.remote.PhotoPagingSource
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que provee el flujo de datos paginados
 */
class PhotoRepository(
    private val api: PhotoApi,
    private val useMockData: Boolean = false
) {

    /**
     * Configuración de paginación optimizada:
     * - pageSize: 20 items por página (balance entre UX y rendimiento)
     * - prefetchDistance: 5 items antes del final para carga anticipada
     * - initialLoadSize: 40 items para llenar pantalla inicial
     */
    fun getPhotosStream(): Flow<PagingData<Photo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                initialLoadSize = 40,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PhotoPagingSource(api, useMockData)
            }
        ).flow
    }

    companion object {
        @Volatile
        private var instance: PhotoRepository? = null

        fun getInstance(useMockData: Boolean = false): PhotoRepository {
            return instance ?: synchronized(this) {
                instance ?: PhotoRepository(
                    PhotoApi.create(),
                    useMockData
                ).also { instance = it }
            }
        }
    }
}