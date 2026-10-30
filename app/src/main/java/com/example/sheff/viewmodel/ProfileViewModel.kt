package com.example.sheff.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.repository.FavoriteRepository
import com.example.sheff.data.repository.RecipeRepository
import com.example.sheff.data.repository.UserRepository
import com.example.sheff.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val favorites: List<Recipe> = emptyList()
)

class ProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val favoriteRepository = FavoriteRepository()
    private val recipeRepository = RecipeRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Введите логин и пароль") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val user = userRepository.login(login, password)
                if (user == null) {
                    _uiState.update { it.copy(isSubmitting = false, error = "Неверный логин или пароль") }
                } else {
                    SessionManager.signIn(user)
                    _uiState.update { it.copy(isSubmitting = false, error = null) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = "Не удалось войти. Проверьте интернет.")
                }
            }
        }
    }

    fun register(login: String, password: String) {
        if (login.isBlank()) {
            _uiState.update { it.copy(error = "Введите логин") }
            return
        }
        if (password.length < 4) {
            _uiState.update { it.copy(error = "Пароль должен быть не короче 4 символов") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val user = userRepository.register(login, password)

                SessionManager.signIn(user)
                _uiState.update { it.copy(isSubmitting = false, error = null) }
            } catch (e: IllegalStateException) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = e.message ?: "Такой логин уже занят")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = "Не удалось зарегистрироваться. Проверьте интернет.")
                }
            }
        }
    }

    fun logout() {
        SessionManager.signOut()
        _uiState.value = ProfileUiState()
    }


    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }


    fun loadFavorites(userId: String?) {
        if (userId == null) {
            _uiState.update { it.copy(favorites = emptyList(), isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val favoriteIds = favoriteRepository.getFavoriteRecipeIds(userId)
                val recipes = if (favoriteIds.isEmpty()) {
                    emptyList()
                } else {

                    recipeRepository.getAllRecipes().filter { it.id in favoriteIds }
                }
                _uiState.update { it.copy(isLoading = false, favorites = recipes) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, favorites = emptyList())
                }
            }
        }
    }
}
