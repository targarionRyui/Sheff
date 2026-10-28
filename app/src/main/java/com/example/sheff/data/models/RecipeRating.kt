package com.example.sheff.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RecipeRating(
    @SerialName("RecipeID") val recipeId: String,
    @SerialName("UserID") val userId: String,
    @SerialName("Rating") val rating: Int
)
