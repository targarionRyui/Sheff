package com.example.sheff.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector


sealed class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : TopDestination("home", "Главная", Icons.Filled.Home)
    data object Catalog : TopDestination("catalog", "Каталог", Icons.Filled.Search)
    data object Profile : TopDestination("profile", "Профиль", Icons.Filled.Person)

    companion object {
        val items: List<TopDestination> = listOf(Home, Catalog, Profile)


        fun topLevelRoute(route: String?): TopDestination? = items.firstOrNull { destination ->
            route == destination.route || route?.startsWith(destination.route + "?") == true
        }
    }
}


object Routes {
    const val RECIPE_ID_ARG = "recipeId"
    const val RECIPE_DETAIL = "recipe/{$RECIPE_ID_ARG}"

    fun recipeDetail(recipeId: String): String = "recipe/$recipeId"

    const val INGREDIENT_ID_ARG = "ingredientId"
    const val INGREDIENT_DETAIL = "ingredient/{$INGREDIENT_ID_ARG}"

    fun ingredientDetail(ingredientId: String): String = "ingredient/$ingredientId"


    const val CATALOG_CATEGORY_ARG = "categoryId"
    const val CATALOG = "catalog?categoryId={$CATALOG_CATEGORY_ARG}"

    fun catalog(categoryId: String? = null): String =
        if (categoryId.isNullOrBlank()) "catalog" else "catalog?categoryId=$categoryId"
}
