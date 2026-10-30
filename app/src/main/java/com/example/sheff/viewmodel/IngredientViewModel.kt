package com.example.sheff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheff.data.models.Ingredient
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.repository.IngredientRepository
import com.example.sheff.data.repository.RecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientUiState(
    val isLoading: Boolean = true,
    val ingredient: Ingredient? = null,
    val recipes: List<Recipe> = emptyList(),
    val error: String? = null
)

class IngredientViewModel : ViewModel() {

    private val ingredientRepository = IngredientRepository()
    private val recipeRepository = RecipeRepository()

    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    private var loadedId: String? = null
    private var lastId: String = ""


    fun load(ingredientId: String) {
        if (loadedId == ingredientId) return
        loadedId = ingredientId
        lastId = ingredientId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val (ingredient, links) = coroutineScope {
                    val ingredientJob = async { ingredientRepository.getIngredient(ingredientId) }
                    val linksJob = async { ingredientRepository.getLinksForIngredient(ingredientId) }
                    ingredientJob.await() to linksJob.await()
                }

                val recipeIds = links.map { it.recipeId }.toSet()
                val recipes = recipeRepository.getAllRecipes().filter { it.id in recipeIds }

                _uiState.value = IngredientUiState(
                    isLoading = false,
                    ingredient = ingredient,
                    recipes = recipes,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Не удалось загрузить ингредиент")
                }
            }
        }
    }

    fun retry() {
        val id = lastId
        loadedId = null
        if (id.isNotEmpty()) load(id)
    }
}
