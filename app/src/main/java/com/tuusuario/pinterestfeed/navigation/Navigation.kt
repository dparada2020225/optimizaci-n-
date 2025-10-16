package com.tuusuario.pinterestfeed.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuusuario.pinterestfeed.data.model.Photo
import com.tuusuario.pinterestfeed.ui.screens.detail.DetailScreen
import com.tuusuario.pinterestfeed.ui.screens.feed.FeedScreen

/**
 * Rutas de navegación
 */
sealed class Screen(val route: String) {
    object Feed : Screen("feed")

    // Segmentos con placeholders (mantenemos el formato original)
    object Detail : Screen(
        "detail/{photoId}/{photoUrl}/{photoWidth}/{photoHeight}/{photoTitle}/{photoAuthor}"
    ) {
        // ✅ Codificamos los valores que pueden contener '/' o espacios
        fun createRoute(photo: Photo): String {
            val encUrl = Uri.encode(photo.url)
            val encTitle = Uri.encode(photo.title ?: "")
            val encAuthor = Uri.encode(photo.author ?: "")
            return "detail/${photo.id}/$encUrl/${photo.width}/${photo.height}/$encTitle/$encAuthor"
        }
    }
}

/**
 * Configuración de navegación de la app
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Feed.route
    ) {
        // Feed principal
        composable(Screen.Feed.route) {
            FeedScreen(
                onPhotoClick = { photo ->
                    navController.navigate(Screen.Detail.createRoute(photo))
                }
            )
        }

        // Detalle de foto
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("photoId") { type = NavType.StringType },
                navArgument("photoUrl") { type = NavType.StringType },
                navArgument("photoWidth") { type = NavType.IntType },
                navArgument("photoHeight") { type = NavType.IntType },
                navArgument("photoTitle") { type = NavType.StringType },
                navArgument("photoAuthor") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // ✅ Decodificamos los strings codificados en la ruta
            val id = backStackEntry.arguments?.getString("photoId").orEmpty()
            val url = Uri.decode(backStackEntry.arguments?.getString("photoUrl").orEmpty())
            val width = backStackEntry.arguments?.getInt("photoWidth") ?: 0
            val height = backStackEntry.arguments?.getInt("photoHeight") ?: 0
            val title = Uri.decode(backStackEntry.arguments?.getString("photoTitle").orEmpty())
            val author = Uri.decode(backStackEntry.arguments?.getString("photoAuthor").orEmpty())

            val photo = Photo(
                id = id,
                url = url,
                width = width,
                height = height,
                title = title,
                author = author
            )

            DetailScreen(
                photo = photo,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
