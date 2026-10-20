package com.example.sheff.data.repository

import com.example.sheff.data.models.Recipe
import com.example.sheff.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class RecipeRepository {

    // Ключевое слово suspend означает, что это асинхронная функция (аналог async в C#).
    // Она не заблокирует главный поток, пока идет запрос по сети.
    suspend fun getAllRecipes(): List<Recipe> {
        return SupabaseClient.client.postgrest["Recipes"]
            .select() // Делаем SELECT * FROM Recipes
            .decodeList<Recipe>() // Превращаем ответ JSON в список наших объектов Recipe
    }
}