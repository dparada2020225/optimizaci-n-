package com.tuusuario.pinterestfeed.data.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tuusuario.pinterestfeed.data.model.Photo
import retrofit2.HttpException
import java.io.IOException

/**
 * PagingSource para carga incremental de fotos
 * Maneja estados de carga, error y retry
 */
class `PhotoPagingSource.kt`(
    private val api: PhotoApi,
    private val useMockData: Boolean = false
) : PagingSource<Int, Photo>() {

    private val mockApi = MockPhotoApi()
    private val tag = "PhotoPagingSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val page = params.key ?: 0
        val loadSize = params.loadSize

        Log.d(tag, "Loading page $page with size $loadSize")

        return try {
            val photos = if (useMockData) {
                // Usar datos mock para testing sin internet
                mockApi.getPhotos(page, loadSize)
            } else {
                // Usar API real de Picsum
                api.getPhotos(page, loadSize).map { it.toPhoto() }
            }

            Log.d(tag, "Successfully loaded ${photos.size} photos for page $page")

            LoadResult.Page(
                data = photos,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (photos.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            Log.e(tag, "Network error loading page $page", e)
            LoadResult.Error(e)
        } catch (e: HttpException) {
            Log.e(tag, "HTTP error loading page $page", e)
            LoadResult.Error(e)
        } catch (e: Exception) {
            Log.e(tag, "Unknown error loading page $page", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        // Restaurar la posición más cercana al scroll actual
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}