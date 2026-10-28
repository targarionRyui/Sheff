package com.example.sheff.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Favorite(
    @SerialName("UserID") val userId: String,
    @SerialName("RecipeID") val recipeId: String,
    @SerialName("UserFavoritiesID") val favoriteId: String? = null
)
