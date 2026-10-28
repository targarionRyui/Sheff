package com.example.sheff.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sheff.ui.screens.CatalogScreen
import com.example.sheff.ui.screens.HomeScreen
import com.example.sheff.ui.screens.IngredientDetailScreen
import com.example.sheff.ui.screens.ProfileScreen
import com.example.sheff.ui.screens.RecipeDetailScreen
import com.example.sheff.viewmodel.HomeViewModel


@Composable
fun SheffNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topDestination = TopDestination.topLevelRoute(currentRoute)
    val showBottomBar = topDestination != null



    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,


        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                SheffBottomBar(

                    currentRoute = topDestination?.route,
                    onDestinationClick = { destination ->


                        if (destination.route != topDestination?.route) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(TopDestination.Home.route) {
                HomeScreen(
                    onRecipeClick = { recipeId -> navController.navigate(Routes.recipeDetail(recipeId)) },
                    onIngredientClick = { ingredientId -> navController.navigate(Routes.ingredientDetail(ingredientId)) },
                    onCategoryClick = { categoryId -> navController.navigate(Routes.catalog(categoryId)) },
                    onOpenCatalog = {
                        navController.navigate(Routes.catalog()) {
                            launchSingleTop = true
                        }
                    },
                    viewModel = homeViewModel
                )
            }
            composable(
                route = Routes.CATALOG,
                arguments = listOf(
                    navArgument(Routes.CATALOG_CATEGORY_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                CatalogScreen(
                    categoryId = entry.arguments?.getString(Routes.CATALOG_CATEGORY_ARG),
                    onRecipeClick = { recipeId -> navController.navigate(Routes.recipeDetail(recipeId)) },
                    favoriteIds = homeState.favoriteIds,
                    onFavoriteToggle = homeViewModel::toggleFavorite,
                    favoriteMessage = homeState.message,
                    onFavoriteMessageShown = homeViewModel::clearMessage,
                    onRefreshFavorites = homeViewModel::loadFavorites
                )
            }
            composable(TopDestination.Profile.route) {
                ProfileScreen(
                    onRecipeClick = { recipeId -> navController.navigate(Routes.recipeDetail(recipeId)) }
                )
            }
            composable(
                route = Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument(Routes.RECIPE_ID_ARG) { type = NavType.StringType })
            ) { entry ->
                RecipeDetailScreen(
                    recipeId = entry.arguments?.getString(Routes.RECIPE_ID_ARG).orEmpty(),
                    onBack = { navController.popBackStack() },

                    // нажмёт «назад» и вернётся к тому же рецепту.
                    onOpenProfile = {
                        navController.navigate(TopDestination.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.INGREDIENT_DETAIL,
                arguments = listOf(navArgument(Routes.INGREDIENT_ID_ARG) { type = NavType.StringType })
            ) { entry ->
                IngredientDetailScreen(
                    ingredientId = entry.arguments?.getString(Routes.INGREDIENT_ID_ARG).orEmpty(),
                    onRecipeClick = { recipeId -> navController.navigate(Routes.recipeDetail(recipeId)) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
