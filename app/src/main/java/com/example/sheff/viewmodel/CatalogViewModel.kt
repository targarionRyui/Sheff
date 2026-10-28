package com.example.sheff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheff.data.models.Category
import com.example.sheff.data.models.Ingredient
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.models.RecipeIngredient
import com.example.sheff.data.models.RecipeRating
import com.example.sheff.data.repository.IngredientRepository
import com.example.sheff.data.repository.RecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class CatalogFilters(
    val categoryId: String? = null,
    val ingredientIds: Set<String> = emptySet(),


    val strictIngredients: Boolean = false,
    val difficulties: Set<String> = emptySet(),
    val timeFrom: Int? = null,
    val timeTo: Int? = null
) {
    /** Сколько групп фильтров включено — показываем цифру на кнопке. */
    val activeCount: Int
        get() = listOf(
            categoryId != null,
            ingredientIds.isNotEmpty(),
            difficulties.isNotEmpty(),
            timeFrom != null || timeTo != null
        ).count { it }
}

data class CatalogUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val categories: List<Category> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val filters: CatalogFilters = CatalogFilters(),
    val recipes: List<RecipeCardItem> = emptyList()
)

private data class CatalogData(
    val recipes: Result<List<Recipe>>,
    val categories: Result<List<Category>>,
    val ingredients: Result<List<Ingredient>>,
    val ratings: Result<List<RecipeRating>>,
    val links: Result<List<RecipeIngredient>>
)

class CatalogViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val ingredientRepository = IngredientRepository()

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    // Полный список рецептов и связей "рецепт -> ингредиенты".
    // Фильтрация идёт по ним, а в state.recipes лежит уже отфильтрованный результат.
    private var allRecipes: List<RecipeCardItem> = emptyList()
    private var linksByRecipe: Map<String, Set<String>> = emptyMap()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = coroutineScope {
                    val recipesJob = async { runCatching { recipeRepository.getAllRecipes() } }
                    val categoriesJob = async { runCatching { recipeRepository.getCategories() } }
                    val ingredientsJob = async { runCatching { ingredientRepository.getAllIngredients() } }
                    val ratingsJob = async { runCatching { recipeRepository.getRecipeRatings() } }
                    val linksJob = async { runCatching { recipeRepository.getRecipeIngredients() } }
                    CatalogData(
                        recipes = recipesJob.await(),
                        categories = categoriesJob.await(),
                        ingredients = ingredientsJob.await(),
                        ratings = ratingsJob.await(),
                        links = linksJob.await()
                    )
                }

                val averageRatings = data.ratings.getOrDefault(emptyList())
                    .groupBy { it.recipeId }
                    .mapValues { (_, values) -> values.map { it.rating.toDouble() }.average() }

                allRecipes = data.recipes.getOrDefault(emptyList()).map { recipe ->
                    RecipeCardItem(recipe = recipe, rating = averageRatings[recipe.id])
                }
                linksByRecipe = data.links.getOrDefault(emptyList())
                    .groupBy { it.recipeId }
                    .mapValues { (_, values) -> values.map { it.ingredientId }.toSet() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = data.categories.getOrDefault(emptyList()),
                        ingredients = data.ingredients.getOrDefault(emptyList()),
                        error = data.recipes.exceptionOrNull()?.message
                    )
                }
                recompute()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Не удалось загрузить каталог")
                }
            }
        }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        recompute()
    }

    fun setCategory(categoryId: String?) {
        _uiState.update { it.copy(filters = it.filters.copy(categoryId = categoryId)) }
        recompute()
    }

    fun applyFilters(filters: CatalogFilters) {
        _uiState.update { it.copy(filters = filters) }
        recompute()
    }

    /**
     * Пересчитывает видимый список. Вызывается после любого изменения фильтров/поиска.
     */
    private fun recompute() {
        val state = _uiState.value
        val filters = state.filters
        val query = state.query.trim()

        val filtered = allRecipes.filter { item ->
            val recipe = item.recipe
            val recipeIngredientIds = linksByRecipe[recipe.id].orEmpty()

            val matchesQuery = query.isEmpty() || recipe.name.contains(query, ignoreCase = true)
            val matchesCategory = filters.categoryId == null || recipe.categoryId == filters.categoryId

            val matchesDifficulty = filters.difficulties.isEmpty() ||
                recipe.difficulty?.trim()?.lowercase() in filters.difficulties

            val cookingTime = recipe.cookingTime
            val matchesTimeFrom = filters.timeFrom == null ||
                (cookingTime != null && cookingTime >= filters.timeFrom)
            val matchesTimeTo = filters.timeTo == null ||
                (cookingTime != null && cookingTime <= filters.timeTo)

            val matchesIngredients = filters.ingredientIds.isEmpty() || if (filters.strictIngredients) {
                recipeIngredientIds.containsAll(filters.ingredientIds)
            } else {
                recipeIngredientIds.any { it in filters.ingredientIds }
            }

            matchesQuery && matchesCategory && matchesDifficulty &&
                matchesTimeFrom && matchesTimeTo && matchesIngredients
        }

        _uiState.update { it.copy(recipes = filtered) }
    }
}
