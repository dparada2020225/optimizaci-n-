package com.tuusuario.pinterestfeed.data.remote

import com.tuusuario.pinterestfeed.data.model.Photo
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * API Service - Usaremos Lorem Picsum (gratuito, sin API key)
 */
interface PhotoApi {

    @GET("v2/list")
    suspend fun getPhotos(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): List<PicsumPhoto>

    companion object {
        private const val BASE_URL = "https://picsum.photos/"

        fun create(): PhotoApi {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PhotoApi::class.java)
        }
    }
}

/**
 * Response de Picsum API
 */
data class PicsumPhoto(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String
)

/**
 * Mapper de PicsumPhoto a nuestro modelo Photo
 */
fun PicsumPhoto.toPhoto(): Photo {
    return Photo(
        id = id,
        url = "https://picsum.photos/id/$id",
        width = width,
        height = height,
        title = "Photo by $author",
        author = author
    )
}

/**
 * Implementación simulada para testing sin red
 */
class MockPhotoApi {
    private val titles = listOf(
        "Mountain Landscape", "City Lights", "Ocean Sunset",
        "Forest Path", "Desert Dunes", "Urban Architecture",
        "Tropical Beach", "Northern Lights", "Autumn Colors",
        "Winter Wonderland", "Spring Flowers", "Summer Vibes"
    )

    private val authors = listOf(
        "Alex Johnson", "Maria Garcia", "John Smith",
        "Emma Wilson", "Carlos Rodriguez", "Sophie Chen"
    )

    suspend fun getPhotos(page: Int, limit: Int = 20): List<Photo> {
        // Simular latencia de red
        delay(500L)

        val startIndex = page * limit
        return (startIndex until startIndex + limit).map { index ->
            val randomHeight = (400..800).random()
            val randomWidth = (300..600).random()

            Photo(
                id = "photo_$index",
                url = "https://picsum.photos/id/${index % 100}",
                width = randomWidth,
                height = randomHeight,
                title = titles[index % titles.size],
                author = authors[index % authors.size]
            )
        }
    }
}