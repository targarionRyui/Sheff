package com.example.sheff.data.repository

import com.example.sheff.data.models.Favorite
import com.example.sheff.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest


class FavoriteRepository {

    suspend fun getFavoriteRecipeIds(userId: String): List<String> {
        return SupabaseClient.client.postgrest["UserFavorities"]
            .select {
                filter { eq("UserID", userId) }
            }
            .decodeList<Favorite>()
            .map { it.recipeId }
    }

    suspend fun add(userId: String, recipeId: String) {
        SupabaseClient.client.postgrest["UserFavorities"]
            .insert(Favorite(userId = userId, recipeId = recipeId))
    }

    suspend fun remove(userId: String, recipeId: String) {
        SupabaseClient.client.postgrest["UserFavorities"].delete {
            filter {
                eq("UserID", userId)
                eq("RecipeID", recipeId)
            }
        }
    }
}
