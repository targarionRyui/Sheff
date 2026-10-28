package com.example.sheff.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.sheff.ui.components.ArchHeader
import com.example.sheff.ui.components.EmptyState
import com.example.sheff.ui.components.ErrorState
import com.example.sheff.ui.components.LoadingState
import com.example.sheff.ui.components.RecipeRow
import com.example.sheff.ui.components.SectionHeader
import com.example.sheff.ui.components.recipeSubtitle
import com.example.sheff.ui.theme.BrownText
import com.example.sheff.ui.theme.CardGray
import com.example.sheff.viewmodel.IngredientViewModel


@Composable
fun IngredientDetailScreen(
    ingredientId: String,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: IngredientViewModel = viewModel()
) {
    LaunchedEffect(ingredientId) {
        viewModel.load(ingredientId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ingredient = state.ingredient
    val error = state.error

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            ArchHeader(
                title = ingredient?.name ?: "Ингредиент",
                onBackClick = onBack
            )
        }

        when {
            state.isLoading -> item { LoadingState() }

            error != null -> item {
                ErrorState(message = error, onRetry = viewModel::retry)
            }

            ingredient != null -> {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(CardGray)
                        ) {
                            if (!ingredient.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ingredient.imageUrl,
                                    contentDescription = ingredient.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }

                        if (!ingredient.description.isNullOrBlank()) {
                            Text(
                                text = ingredient.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = BrownText
                            )
                        }
                    }
                }

                item {
                    SectionHeader(title = "Рецепты с этим ингредиентом")
                }

                if (state.recipes.isEmpty()) {
                    item { EmptyState(text = "Пока нет рецептов с этим ингредиентом") }
                } else {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.recipes.forEach { recipe ->
                                RecipeRow(
                                    title = recipe.name,
                                    subtitle = recipeSubtitle(recipe.cookingTime, recipe.difficulty),
                                    imageUrl = recipe.imageUrl,
                                    onClick = { onRecipeClick(recipe.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
