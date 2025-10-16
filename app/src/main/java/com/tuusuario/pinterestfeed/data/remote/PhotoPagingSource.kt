package com.tuusuario.pinterestfeed.data.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tuusuario.pinterestfeed.data.model.Photo
import retrofit2.HttpException
import java.io.IOException

class PhotoPagingSource(
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
                mockApi.getPhotos(page, loadSize)
            } else {
                api.getPhotos(page, loadSize).map { it.toPhoto() }
            }

            LoadResult.Page(
                data = photos,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (photos.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }
}
