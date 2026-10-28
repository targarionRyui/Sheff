package com.example.sheff.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.sheff.ui.components.CatalogFilterSheet
import com.example.sheff.ui.components.EmptyState
import com.example.sheff.ui.components.ErrorState
import com.example.sheff.ui.components.LoadingState
import com.example.sheff.ui.components.RecipeCard
import com.example.sheff.ui.components.SearchField
import com.example.sheff.ui.theme.InkBlack
import com.example.sheff.ui.theme.Yellow
import com.example.sheff.viewmodel.CatalogViewModel


@Composable
fun CatalogScreen(
    categoryId: String?,
    onRecipeClick: (String) -> Unit,
    favoriteIds: Set<String>,
    onFavoriteToggle: (String) -> Unit,
    favoriteMessage: String?,
    onFavoriteMessageShown: () -> Unit,
    onRefreshFavorites: (String?) -> Unit,
    viewModel: CatalogViewModel = viewModel()
) {
    LaunchedEffect(categoryId) {
        if (!categoryId.isNullOrBlank()) {
            viewModel.setCategory(categoryId)
        }
    }

    val currentUser by SessionManager.currentUser.collectAsStateWithLifecycle()
    LaunchedEffect(currentUser?.id) {
        onRefreshFavorites(currentUser?.id)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(favoriteMessage) {
        val text = favoriteMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        onFavoriteMessageShown()
    }

    val error = state.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ArchHeader(title = "Рецепты") {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = "Название рецепта..."
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    state.isLoading -> LoadingState(Modifier.align(Alignment.Center))

                    error != null -> ErrorState(
                        message = error,
                        onRetry = viewModel::load,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    state.recipes.isEmpty() -> EmptyState(
                        text = "Ничего не нашлось. Попробуй изменить фильтры.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 16.dp,
                            end = 20.dp,
                            bottom = 110.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.recipes, key = { it.recipe.id }) { item ->
                            RecipeCard(
                                title = item.recipe.name,
                                imageUrl = item.recipe.imageUrl,
                                rating = item.rating,
                                isFavorite = item.recipe.id in favoriteIds,
                                onFavoriteClick = { onFavoriteToggle(item.recipe.id) },
                                onClick = { onRecipeClick(item.recipe.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(232.dp)
                            )
                        }
                    }
                }
            }
        }


        Button(
            onClick = { showFilters = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = InkBlack
            ),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text(
                text = if (state.filters.activeCount > 0) {
                    "Фильтры · ${state.filters.activeCount}"
                } else {
                    "Фильтры"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
        )
    }

    if (showFilters) {
        CatalogFilterSheet(
            filters = state.filters,
            categories = state.categories,
            ingredients = state.ingredients,
            onDismiss = { showFilters = false },
            onApply = { newFilters ->
                viewModel.applyFilters(newFilters)
                showFilters = false
            }
        )
    }
}
