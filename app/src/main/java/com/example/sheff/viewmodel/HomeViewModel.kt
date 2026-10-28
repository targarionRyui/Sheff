package com.example.sheff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheff.data.models.Category
import com.example.sheff.data.models.Ingredient
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.models.RecipeRating
import com.example.sheff.data.repository.FavoriteRepository
import com.example.sheff.data.repository.IngredientRepository
import com.example.sheff.data.repository.RecipeRepository
import com.example.sheff.data.session.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RecipeCardItem(
    val recipe: Recipe,
    val rating: Double?
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val recipes: List<RecipeCardItem> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val error: String? = null,

    val favoriteIds: Set<String> = emptySet(),

    val message: String? = null
)

private data class HomeData(
    val categories: Result<List<Category>>,
    val recipes: Result<List<Recipe>>,
    val ratings: Result<List<RecipeRating>>,
    val ingredients: Result<List<Ingredient>>
)

class HomeViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val ingredientRepository = IngredientRepository()
    private val favoriteRepository = FavoriteRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Четыре запроса параллельно. Каждый в runCatching:
                // падение рейтингов или ингредиентов не должно ломать весь экран.
                val data = coroutineScope {
                    val categoriesJob = async { runCatching { recipeRepository.getCategories() } }
                    val recipesJob = async { runCatching { recipeRepository.getAllRecipes() } }
                    val ratingsJob = async { runCatching { recipeRepository.getRecipeRatings() } }
                    val ingredientsJob = async { runCatching { ingredientRepository.getAllIngredients() } }
                    HomeData(
                        categories = categoriesJob.await(),
                        recipes = recipesJob.await(),
                        ratings = ratingsJob.await(),
                        ingredients = ingredientsJob.await()
                    )
                }

                val categoriesResult = data.categories
                val recipesResult = data.recipes
                val ratingsResult = data.ratings
                val ingredientsResult = data.ingredients

                val averageRatings = ratingsResult.getOrDefault(emptyList())
                    .groupBy { it.recipeId }
                    .mapValues { (_, values) -> values.map { it.rating.toDouble() }.average() }

                // copy, а не новый HomeUiState: иначе при повторной загрузке
                // потерялись бы избранное и текст сообщения.
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        categories = categoriesResult.getOrDefault(emptyList()),
                        recipes = recipesResult.getOrDefault(emptyList()).map { recipe ->
                            RecipeCardItem(recipe = recipe, rating = averageRatings[recipe.id])
                        },
                        ingredients = ingredientsResult.getOrDefault(emptyList()),
                        error = recipesResult.exceptionOrNull()?.message
                            ?: categoriesResult.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить данные"
                    )
                }
            }
        }
    }

    /** Подтягивает избранное пользователя. Вызывается при входе и открытии главной/каталога. */
    fun loadFavorites(userId: String?) {
        if (userId == null) {
            _uiState.update { it.copy(favoriteIds = emptySet()) }
            return
        }

        viewModelScope.launch {
            val ids = runCatching { favoriteRepository.getFavoriteRecipeIds(userId) }
                .getOrDefault(emptyList())
            _uiState.update { it.copy(favoriteIds = ids.toSet()) }
        }
    }

    /** Сердечко на карточке рецепта. */
    fun toggleFavorite(recipeId: String) {
        val user = SessionManager.currentUser.value
        if (user == null) {
            _uiState.update { it.copy(message = "Войдите в аккаунт, чтобы сохранять рецепты") }
            return
        }

        val wasFavorite = recipeId in _uiState.value.favoriteIds
        viewModelScope.launch {
            try {
                if (wasFavorite) {
                    favoriteRepository.remove(user.id, recipeId)
                } else {
                    favoriteRepository.add(user.id, recipeId)
                }
                _uiState.update { current ->
                    current.copy(
                        favoriteIds = if (wasFavorite) {
                            current.favoriteIds - recipeId
                        } else {
                            current.favoriteIds + recipeId
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = "Не удалось обновить избранное. Проверьте интернет.")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
