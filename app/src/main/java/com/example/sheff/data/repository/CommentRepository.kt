package com.example.sheff.data.repository

import com.example.sheff.data.models.Comment
import com.example.sheff.data.models.User
import com.example.sheff.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class CommentRepository {

    suspend fun getCommentsForRecipe(recipeId: String): List<Comment> {
        return SupabaseClient.client.postgrest["Comments"]
            .select {
                filter { eq("RecipeID", recipeId) }
            }
            .decodeList<Comment>()
    }


    suspend fun addComment(recipeId: String, user: User, text: String): Comment {
        val comment = Comment(
            id = UUID.randomUUID().toString(),
            recipeId = recipeId,
            userId = user.id,
            userLogin = user.login,
            text = text,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        )
        SupabaseClient.client.postgrest["Comments"].insert(comment)
        return comment
    }
}
