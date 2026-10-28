package com.example.sheff.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sheff.data.models.Comment
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.session.SessionManager
import com.example.sheff.ui.components.ArchHeader
import com.example.sheff.ui.components.EmptyState
import com.example.sheff.ui.components.ErrorState
import com.example.sheff.ui.components.LoadingState
import com.example.sheff.ui.components.SectionHeader
import com.example.sheff.ui.components.commentDateLabel
import com.example.sheff.ui.components.cookingTimeLabel
import com.example.sheff.ui.components.difficultyLabel
import com.example.sheff.ui.components.ingredientAmountLabel
import com.example.sheff.ui.components.ratingLabel
import com.example.sheff.ui.components.ratingsCountLabel
import com.example.sheff.ui.theme.BrownText
import com.example.sheff.ui.theme.CardGray
import com.example.sheff.ui.theme.HeartRed
import com.example.sheff.ui.theme.MintText
import com.example.sheff.ui.theme.Yellow
import com.example.sheff.viewmodel.RecipeDetailViewModel
import com.example.sheff.viewmodel.RecipeIngredientItem
import kotlin.math.roundToInt


@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel()
) {
    val currentUser by SessionManager.currentUser.collectAsStateWithLifecycle()



    LaunchedEffect(recipeId, currentUser?.id) {
        viewModel.load(recipeId, currentUser)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val text = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.clearMessage()
    }

    val recipe = state.recipe
    val error = state.error

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                ArchHeader(title = "Рецепт", onBackClick = onBack)
            }

            when {
                state.isLoading -> item { LoadingState() }

                error != null -> item {
                    ErrorState(
                        message = error,
                        onRetry = { viewModel.load(recipeId, currentUser) }
                    )
                }

                recipe != null -> {
                    item {
                        RecipeHero(
                            recipe = recipe,
                            averageRating = state.averageRating,
                            isFavorite = state.isFavorite,
                            onFavoriteClick = viewModel::toggleFavorite
                        )
                    }

                    item { SectionHeader(title = "Ингредиенты", modifier = Modifier.padding(top = 22.dp)) }

                    if (state.ingredients.isEmpty()) {
                        item { EmptyState(text = "Состав пока не указан") }
                    } else {
                        item { IngredientsCard(state.ingredients) }
                    }

                    item { SectionHeader(title = "Оценки", modifier = Modifier.padding(top = 22.dp)) }

                    item {
                        RatingCard(
                            averageRating = state.averageRating,
                            ratingsCount = state.ratingsCount,
                            myRating = state.myRating,
                            onRate = viewModel::rate
                        )
                    }

                    item { SectionHeader(title = "Комментарии", modifier = Modifier.padding(top = 22.dp)) }

                    if (currentUser != null) {
                        item {
                            CommentComposer(
                                draft = state.commentDraft,
                                onDraftChange = viewModel::onCommentChange,
                                onSend = viewModel::sendComment
                            )
                        }
                    }

                    if (state.comments.isEmpty()) {
                        item { EmptyState(text = "Пока нет комментариев") }
                    } else {
                        items(state.comments, key = { it.id }) { comment ->
                            CommentRow(comment)
                        }
                    }

                    if (currentUser == null) {
                        item { LoginHint(onOpenProfile) }
                    }
                }
            }
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
private fun RecipeHero(
    recipe: Recipe,
    averageRating: Double?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(CardGray)
        ) {
            if (!recipe.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable(onClick = onFavoriteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
                    tint = if (isFavorite) HeartRed else BrownText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = recipe.name,
            style = MaterialTheme.typography.headlineMedium,
            color = BrownText
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val time = cookingTimeLabel(recipe.cookingTime)
            if (time.isNotBlank()) MetaPill(time)
            MetaPill(difficultyLabel(recipe.difficulty))
            if (averageRating != null) MetaPill("★ ${ratingLabel(averageRating)}")
        }

        if (!recipe.description.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyLarge,
                color = BrownText
            )
        }
    }
}


@Composable
private fun MetaPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = BrownText
        )
    }
}


@Composable
private fun IngredientsCard(ingredients: List<RecipeIngredientItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrownText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ingredientAmountLabel(ingredient.amount, ingredient.unit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintText
                )
            }
        }
    }
}


@Composable
private fun RatingCard(
    averageRating: Double?,
    ratingsCount: Int,
    myRating: Int?,
    onRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingStars(rating = averageRating, onRate = onRate)
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (averageRating == null) {
                    "Пока нет оценок"
                } else {
                    "${ratingLabel(averageRating)} · ${ratingsCountLabel(ratingsCount)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = BrownText
            )
        }

        if (myRating != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ваша оценка: $myRating",
                style = MaterialTheme.typography.labelLarge,
                color = MintText
            )
        }
    }
}


@Composable
private fun RatingStars(rating: Double?, onRate: (Int) -> Unit) {
    val filledCount = rating?.roundToInt() ?: 0

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            val value = index + 1
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Оценка $value",
                tint = if (index < filledCount) Yellow else CardGray,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onRate(value) }
            )
        }
    }
}


@Composable
private fun CommentComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            placeholder = { Text("Написать комментарий…") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onSend,
            enabled = draft.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = BrownText
            ),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Отправить", style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** Один комментарий: кружок с первой буквой, логин, дата и текст. */
@Composable
private fun CommentRow(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.userLogin.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = BrownText
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userLogin,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrownText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = commentDateLabel(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MintText
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = BrownText
            )
        }
    }
}

/** Плашка «войди, чтобы оценить» для гостя. */
@Composable
private fun LoginHint(onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Чтобы поставить оценку или написать комментарий, войди в аккаунт",
            style = MaterialTheme.typography.bodyMedium,
            color = MintText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onOpenProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = BrownText
            )
        ) {
            Text("Войти в аккаунт", style = MaterialTheme.typography.titleMedium)
        }
    }
}
