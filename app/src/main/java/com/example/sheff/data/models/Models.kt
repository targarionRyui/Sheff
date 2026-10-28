package com.example.sheff.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable




@Serializable
data class User(
    @SerialName("UserID") val id: String,
    @SerialName("UserLogin") val login: String,
    @SerialName("UserPassword") val password: String
)

@Serializable
data class Recipe(
    @SerialName("RecipeId") val id: String,
    @SerialName("RecipeName") val name: String,
    @SerialName("RecipeDescription") val description: String?,
    @SerialName("BaseServings") val servings: Int?,
    @SerialName("CookingTime") val cookingTime: Int?,
    @SerialName("Difficulty") val difficulty: String?,
    @SerialName("CategoryID") val categoryId: String?,
    @SerialName("ImageURL") val imageUrl: String?
)

@Serializable
data class Category(
    @SerialName("CategoryID") val id: String,
    @SerialName("CategoryName") val name: String,
    @SerialName("CategoryImage") val imageUrl: String?,
    @SerialName("CategoryDescription") val description: String?
)

@Serializable


data class Ingredient(
    @SerialName("IngridientID") val id: String,
    @SerialName("IngridientName") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("ImageURL") val imageUrl: String?
)

@Serializable
data class Comment(
    @SerialName("CommentID") val id: String,
    @SerialName("RecipeID") val recipeId: String,
    @SerialName("UserID") val userId: String,
    @SerialName("UserLogin") val userLogin: String,
    @SerialName("CommentText") val text: String,
    @SerialName("CreatedAt") val createdAt: String? = null
)


@Serializable
data class RecipeIngredient(
    @SerialName("LinkID") val id: String,
    @SerialName("RecipeID") val recipeId: String,
    @SerialName("IngredientID") val ingredientId: String,
    @SerialName("Amount") val amount: Float?,
    @SerialName("Unit") val unit: String?
)