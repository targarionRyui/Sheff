package com.example.sheff.data.repository

import com.example.sheff.data.models.Ingredient
import com.example.sheff.data.models.RecipeIngredient
import com.example.sheff.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest


class IngredientRepository {

    suspend fun getAllIngredients(): List<Ingredient> {
        return SupabaseClient.client.postgrest["Ingridients"]
            .select()
            .decodeList<Ingredient>()
    }

    suspend fun getIngredient(ingredientId: String): Ingredient {
        return SupabaseClient.client.postgrest["Ingridients"]
            .select {
                filter { eq("IngridientID", ingredientId) }
            }
            .decodeSingle()
    }


    suspend fun getLinksForIngredient(ingredientId: String): List<RecipeIngredient> {
        return SupabaseClient.client.postgrest["RecipeIngredients"]
            .select {
                filter { eq("IngredientID", ingredientId) }
            }
            .decodeList()
    }
}
