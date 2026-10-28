package com.example.sheff.data.repository

import com.example.sheff.data.models.Category
import com.example.sheff.data.models.Recipe
import com.example.sheff.data.models.RecipeIngredient
import com.example.sheff.data.models.RecipeRating
import com.example.sheff.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest


class RecipeRepository {

    suspend fun getAllRecipes(): List<Recipe> {
        return SupabaseClient.client.postgrest["Recipes"]
            .select()
            .decodeList<Recipe>()
    }

    suspend fun getCategories(): List<Category> {
        return SupabaseClient.client.postgrest["Categories"]
            .select()
            .decodeList<Category>()
    }

    suspend fun getRecipe(recipeId: String): Recipe {
        return SupabaseClient.client.postgrest["Recipes"]
            .select {
                filter { eq("RecipeId", recipeId) }
            }
            .decodeSingle()
    }



    suspend fun getRecipeRatings(): List<RecipeRating> {
        return SupabaseClient.client.postgrest["RecipeRatings"]
            .select()
            .decodeList<RecipeRating>()
    }


    suspend fun getRatingsForRecipe(recipeId: String): List<RecipeRating> {
        return SupabaseClient.client.postgrest["RecipeRatings"]
            .select {
                filter { eq("RecipeID", recipeId) }
            }
            .decodeList<RecipeRating>()
    }


    suspend fun getRecipeIngredients(): List<RecipeIngredient> {
        return SupabaseClient.client.postgrest["RecipeIngredients"]
            .select()
            .decodeList<RecipeIngredient>()
    }


    suspend fun getLinksForRecipe(recipeId: String): List<RecipeIngredient> {
        return SupabaseClient.client.postgrest["RecipeIngredients"]
            .select {
                filter { eq("RecipeID", recipeId) }
            }
            .decodeList<RecipeIngredient>()
    }

    /**
     * Сохраняет оценку пользователя.
     * upsert = "вставь, а если такая пара RecipeID+UserID уже есть — обнови".
     * Поэтому один пользователь всегда имеет ровно одну оценку на рецепт.
     */
    suspend fun saveRating(recipeId: String, userId: String, rating: Int) {
        SupabaseClient.client.postgrest["RecipeRatings"].upsert(
            value = RecipeRating(recipeId = recipeId, userId = userId, rating = rating),
            onConflict = "RecipeID,UserID"
        )
    }
}
