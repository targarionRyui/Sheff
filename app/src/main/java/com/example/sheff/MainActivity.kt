package com.example.sheff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sheff.viewmodel.RecipeUiState
import com.example.sheff.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    // Подключаем нашу ViewModel
    private val viewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Собираем состояние из ViewModel (как подписка на событие)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            Surface(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is RecipeUiState.Loading -> {
                        // Экран загрузки
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is RecipeUiState.Success -> {
                        if (state.recipes.isEmpty()) {
                            // Если список пустой, покажем текст
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "База данных вернула 0 рецептов :(", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            // Если есть данные, показываем список
                            LazyColumn(modifier = Modifier.padding(16.dp)) {
                                items(state.recipes) { recipe ->
                                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(text = recipe.name, style = MaterialTheme.typography.titleLarge)
                                            Text(text = "Время: ${recipe.cookingTime} мин.", style = MaterialTheme.typography.bodyMedium)
                                            Text(text = "Сложность: ${recipe.difficulty}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is RecipeUiState.Error -> {
                        // Экран ошибки
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Ошибка: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}