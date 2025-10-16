package com.tuusuario.pinterestfeed.navigation

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
    object Detail : Screen("detail/{photoId}/{photoUrl}/{photoWidth}/{photoHeight}/{photoTitle}/{photoAuthor}") {
        fun createRoute(photo: Photo): String {
            return "detail/${photo.id}/${photo.url}/${photo.width}/${photo.height}/${photo.title}/${photo.author}"
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
            val photo = Photo(
                id = backStackEntry.arguments?.getString("photoId") ?: "",
                url = backStackEntry.arguments?.getString("photoUrl") ?: "",
                width = backStackEntry.arguments?.getInt("photoWidth") ?: 0,
                height = backStackEntry.arguments?.getInt("photoHeight") ?: 0,
                title = backStackEntry.arguments?.getString("photoTitle") ?: "",
                author = backStackEntry.arguments?.getString("photoAuthor") ?: ""
            )

            DetailScreen(
                photo = photo,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}