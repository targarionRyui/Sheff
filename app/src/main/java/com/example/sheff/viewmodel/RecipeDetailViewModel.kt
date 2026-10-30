package com.example.sheff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheff.data.models.Comment
import com.example.sheff.data.models.Ingredient
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.models.RecipeIngredient
import com.example.sheff.data.models.RecipeRating
import com.example.sheff.data.models.User
import com.example.sheff.data.repository.CommentRepository
import com.example.sheff.data.repository.FavoriteRepository
import com.example.sheff.data.repository.IngredientRepository
import com.example.sheff.data.repository.RecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RecipeIngredientItem(
    val name: String,
    val amount: Float?,
    val unit: String?,
    val imageUrl: String?
)

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val recipe: Recipe? = null,
    val ingredients: List<RecipeIngredientItem> = emptyList(),
    val averageRating: Double? = null,
    val ratingsCount: Int = 0,
    val comments: List<Comment> = emptyList(),

    val isFavorite: Boolean = false,
    val myRating: Int? = null,
    val commentDraft: String = "",

    val message: String? = null
)



private data class RecipeDetailData(
    val recipe: Result<Recipe>,
    val links: Result<List<RecipeIngredient>>,
    val ingredients: Result<List<Ingredient>>,
    val ratings: Result<List<RecipeRating>>,
    val comments: Result<List<Comment>>
)

class RecipeDetailViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val ingredientRepository = IngredientRepository()
    private val commentRepository = CommentRepository()
    private val favoriteRepository = FavoriteRepository()

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private var recipeId: String = ""
    private var currentUser: User? = null


    fun load(recipeId: String, user: User?) {
        if (recipeId.isBlank()) return

        this.recipeId = recipeId
        this.currentUser = user

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = coroutineScope {
                    val recipeJob = async { runCatching { recipeRepository.getRecipe(recipeId) } }
                    val linksJob = async { runCatching { recipeRepository.getLinksForRecipe(recipeId) } }
                    val ingredientsJob = async { runCatching { ingredientRepository.getAllIngredients() } }
                    val ratingsJob = async { runCatching { recipeRepository.getRatingsForRecipe(recipeId) } }
                    val commentsJob = async { runCatching { commentRepository.getCommentsForRecipe(recipeId) } }

                    RecipeDetailData(
                        recipe = recipeJob.await(),
                        links = linksJob.await(),
                        ingredients = ingredientsJob.await(),
                        ratings = ratingsJob.await(),
                        comments = commentsJob.await()
                    )
                }

                val recipeResult = data.recipe
                val recipe = recipeResult.getOrNull()


                val ingredientsById = data.ingredients.getOrDefault(emptyList()).associateBy { it.id }
                val ingredientItems = data.links.getOrDefault(emptyList()).mapNotNull { link ->
                    val ingredient = ingredientsById[link.ingredientId] ?: return@mapNotNull null
                    RecipeIngredientItem(
                        name = ingredient.name,
                        amount = link.amount,
                        unit = link.unit,
                        imageUrl = ingredient.imageUrl
                    )
                }

                val ratings = data.ratings.getOrDefault(emptyList())
                val comments = data.comments.getOrDefault(emptyList())
                    .sortedByDescending { it.createdAt ?: "" }


                var isFavorite = false
                var myRating: Int? = null
                if (user != null) {
                    isFavorite = runCatching { favoriteRepository.getFavoriteRecipeIds(user.id) }
                        .getOrDefault(emptyList())
                        .contains(recipeId)
                    myRating = ratings.firstOrNull { it.userId == user.id }?.rating
                }

                _uiState.value = RecipeDetailUiState(
                    isLoading = false,
                    error = if (recipe == null) {
                        recipeResult.exceptionOrNull()?.message ?: "Рецепт не найден"
                    } else {
                        null
                    },
                    recipe = recipe,
                    ingredients = ingredientItems,
                    averageRating = if (ratings.isEmpty()) {
                        null
                    } else {
                        ratings.map { it.rating }.average()
                    },
                    ratingsCount = ratings.size,
                    comments = comments,
                    isFavorite = isFavorite,
                    myRating = myRating
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Не удалось загрузить рецепт")
                }
            }
        }
    }


    fun toggleFavorite() {
        val user = currentUser ?: return showMessage("Войдите в аккаунт, чтобы сохранять рецепты")

        val wasFavorite = _uiState.value.isFavorite
        viewModelScope.launch {
            try {
                if (wasFavorite) {
                    favoriteRepository.remove(user.id, recipeId)
                } else {
                    favoriteRepository.add(user.id, recipeId)
                }
                _uiState.update { it.copy(isFavorite = !wasFavorite) }
            } catch (e: Exception) {
                showMessage("Не удалось обновить избранное. Проверьте интернет.")
            }
        }
    }


    fun rate(value: Int) {
        val user = currentUser ?: return showMessage("Войдите в аккаунт, чтобы оценить рецепт")

        viewModelScope.launch {
            try {
                recipeRepository.saveRating(recipeId, user.id, value)

                val ratings = recipeRepository.getRatingsForRecipe(recipeId)
                _uiState.update {
                    it.copy(
                        myRating = value,
                        averageRating = if (ratings.isEmpty()) null else ratings.map { r -> r.rating }.average(),
                        ratingsCount = ratings.size
                    )
                }
            } catch (e: Exception) {
                showMessage("Не удалось сохранить оценку. Проверьте интернет.")
            }
        }
    }

    fun onCommentChange(text: String) {
        _uiState.update { it.copy(commentDraft = text) }
    }

    fun sendComment() {
        val user = currentUser ?: return showMessage("Войдите в аккаунт, чтобы написать комментарий")
        val text = _uiState.value.commentDraft.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            try {
                val comment = commentRepository.addComment(recipeId, user, text)
                _uiState.update {
                    it.copy(
                        comments = listOf(comment) + it.comments,
                        commentDraft = ""
                    )
                }
            } catch (e: Exception) {
                showMessage("Не удалось отправить комментарий. Проверьте интернет.")
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun showMessage(text: String) {
        _uiState.update { it.copy(message = text) }
    }
}
