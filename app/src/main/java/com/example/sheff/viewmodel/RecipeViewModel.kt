package com.example.sheff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Интерфейс (или sealed class) для описания состояний экрана
sealed interface RecipeUiState {
    object Loading : RecipeUiState
    data class Success(val recipes: List<Recipe>) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

class RecipeViewModel : ViewModel() {
    private val repository = RecipeRepository()

    // Приватное изменяемое состояние (изменять может только ViewModel)
    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)

    // Публичное неизменяемое (UI может только читать)
    val uiState: StateFlow<RecipeUiState> = _uiState

    init {
        fetchRecipes() // Загружаем рецепты при создании ViewModel
    }

    fun fetchRecipes() {
        // viewModelScope.launch — запускает корутину (асинхронную задачу)
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading // Включаем спиннер на экране
            try {
                val recipes = repository.getAllRecipes()
                _uiState.value = RecipeUiState.Success(recipes) // Отдаем данные
            } catch (e: Exception) {
                _uiState.value = RecipeUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}