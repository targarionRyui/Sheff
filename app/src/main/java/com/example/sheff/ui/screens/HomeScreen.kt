package com.example.sheff.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheff.data.session.SessionManager
import com.example.sheff.ui.components.ArchHeader
import com.example.sheff.ui.components.CategoryCard
import com.example.sheff.ui.components.EmptyState
import com.example.sheff.ui.components.ErrorState
import com.example.sheff.ui.components.IngredientRow
import com.example.sheff.ui.components.LoadingState
import com.example.sheff.ui.components.RecipeCard
import com.example.sheff.ui.components.RecipeRow
import com.example.sheff.ui.components.SearchField
import com.example.sheff.ui.components.SectionHeader
import com.example.sheff.ui.components.recipeSubtitle
import com.example.sheff.viewmodel.HomeUiState
import com.example.sheff.viewmodel.HomeViewModel
import com.example.sheff.viewmodel.RecipeCardItem


@Composable
fun HomeScreen(
    onRecipeClick: (String) -> Unit,
    onIngredientClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onOpenCatalog: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by SessionManager.currentUser.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }


    LaunchedEffect(currentUser?.id) {
        viewModel.loadFavorites(currentUser?.id)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val text = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.clearMessage()
    }

    val isSearching = query.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                ArchHeader(title = "Что ты ищешь воин?") {
                    SearchField(
                        value = query,
                        onValueChange = { query = it }
                    )
                }
            }


            item {
                AnimatedVisibility(
                    visible = !isSearching,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    HomeSections(
                        state = state,
                        favoriteIds = state.favoriteIds,
                        onFavoriteToggle = viewModel::toggleFavorite,
                        onRecipeClick = onRecipeClick,
                        onCategoryClick = onCategoryClick,
                        onOpenCatalog = onOpenCatalog,
                        onRetry = viewModel::load
                    )
                }
            }


            item {
                AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SearchResults(
                        state = state,
                        query = query,
                        onIngredientClick = onIngredientClick,
                        onRecipeClick = onRecipeClick,
                        onRetry = viewModel::load
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}


@Composable
private fun HomeSections(
    state: HomeUiState,
    favoriteIds: Set<String>,
    onFavoriteToggle: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onOpenCatalog: () -> Unit,
    onRetry: () -> Unit
) {
    val error = state.error

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(24.dp))

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        if (state.categories.isNotEmpty()) {
            SectionHeader(title = "Категории", onMoreClick = onOpenCatalog)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.categories, key = { it.id }) { category ->
                    CategoryCard(
                        name = category.name,
                        imageUrl = category.imageUrl,
                        onClick = { onCategoryClick(category.id) }
                    )
                }
            }
        }

        SectionHeader(title = "Рецепты", onMoreClick = onOpenCatalog)

        when {
            error != null -> ErrorState(message = error, onRetry = onRetry)

            state.recipes.isEmpty() -> EmptyState(text = "Пока нет рецептов")

            else -> LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.recipes, key = { _, item -> item.recipe.id }) { _, item ->
                    RecipeCard(
                        title = item.recipe.name,
                        imageUrl = item.recipe.imageUrl,
                        rating = item.rating,
                        isFavorite = item.recipe.id in favoriteIds,
                        onFavoriteClick = { onFavoriteToggle(item.recipe.id) },
                        onClick = { onRecipeClick(item.recipe.id) }
                    )
                }
            }
        }
    }
}


@Composable
private fun SearchResults(
    state: HomeUiState,
    query: String,
    onIngredientClick: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    val text = query.trim()

    val matchedIngredients = remember(state.ingredients, text) {
        state.ingredients
            .filter { it.name.contains(text, ignoreCase = true) }
            .take(5)
    }
    val matchedRecipes = remember(state.recipes, text) {
        state.recipes
            .filter { it.recipe.name.contains(text, ignoreCase = true) }
            .take(6)
    }

    val error = state.error

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))

        when {
            state.isLoading -> LoadingState()

            error != null -> ErrorState(message = error, onRetry = onRetry)

            matchedIngredients.isEmpty() && matchedRecipes.isEmpty() -> EmptyState(
                text = "По запросу «$text» ничего не нашлось"
            )

            else -> {
                if (matchedIngredients.isNotEmpty()) {
                    SectionHeader(title = "Ингредиенты")
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matchedIngredients.forEach { ingredient ->
                            IngredientRow(
                                name = ingredient.name,
                                description = ingredient.description,
                                imageUrl = ingredient.imageUrl,
                                onClick = { onIngredientClick(ingredient.id) }
                            )
                        }
                    }
                }

                if (matchedRecipes.isNotEmpty()) {
                    SectionHeader(title = "Рецепты")
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matchedRecipes.forEach { item: RecipeCardItem ->
                            RecipeRow(
                                title = item.recipe.name,
                                subtitle = recipeSubtitle(item.recipe.cookingTime, item.recipe.difficulty),
                                imageUrl = item.recipe.imageUrl,
                                onClick = { onRecipeClick(item.recipe.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
