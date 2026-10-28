package com.example.sheff.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheff.data.session.SessionManager
import com.example.sheff.ui.components.ArchHeader
import com.example.sheff.ui.components.EmptyState
import com.example.sheff.ui.components.LoadingState
import com.example.sheff.ui.components.RecipeRow
import com.example.sheff.ui.components.SectionHeader
import com.example.sheff.ui.components.recipeSubtitle
import com.example.sheff.ui.theme.BrownText
import com.example.sheff.ui.theme.MintText
import com.example.sheff.ui.theme.Yellow
import com.example.sheff.viewmodel.ProfileUiState
import com.example.sheff.viewmodel.ProfileViewModel


@Composable
fun ProfileScreen(
    onRecipeClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by SessionManager.currentUser.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val currentUser = user


    LaunchedEffect(currentUser?.id) {
        viewModel.loadFavorites(currentUser?.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ArchHeader(title = if (currentUser == null) "Вход" else "Аккаунт")

        if (currentUser == null) {
            AuthForm(
                state = state,
                onLogin = viewModel::login,
                onRegister = viewModel::register,
                onClearError = viewModel::clearError
            )
        } else {
            FavoritesContent(
                login = currentUser.login,
                state = state,
                onLogout = viewModel::logout,
                onRecipeClick = onRecipeClick
            )
        }
    }
}

@Composable
private fun AuthForm(
    state: ProfileUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val error = state.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (isRegister) "Создать аккаунт" else "Войти в аккаунт",
            style = MaterialTheme.typography.headlineSmall,
            color = BrownText
        )

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                if (isRegister) {
                    onRegister(login.trim(), password)
                } else {
                    onLogin(login.trim(), password)
                }
            },
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Yellow,
                contentColor = BrownText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    color = BrownText,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = if (isRegister) "Зарегистрироваться" else "Войти",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        TextButton(
            onClick = {
                isRegister = !isRegister
                onClearError()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться",
                color = BrownText
            )
        }

        Text(
            text = "Аккаунты хранятся в таблице Users в Supabase.",
            style = MaterialTheme.typography.labelSmall,
            color = MintText
        )
    }
}

@Composable
private fun FavoritesContent(
    login: String,
    state: ProfileUiState,
    onLogout: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Yellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = login.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = BrownText
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Вы вошли как",
                    style = MaterialTheme.typography.labelMedium,
                    color = MintText
                )
                Text(
                    text = login,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrownText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(onClick = onLogout) {
                Text("Выйти", color = BrownText)
            }
        }

        SectionHeader(title = "Избранное")

        when {
            state.isLoading -> LoadingState()

            state.favorites.isEmpty() -> EmptyState(
                text = "Пока нет избранных рецептов.\nОткрой рецепт и нажми на сердечко."
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.favorites, key = { it.id }) { recipe ->
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
