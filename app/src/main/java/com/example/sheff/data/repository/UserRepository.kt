package com.example.sheff.data.repository

import com.example.sheff.data.models.User
import com.example.sheff.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID


class UserRepository {


    suspend fun login(login: String, password: String): User? {
        return SupabaseClient.client.postgrest["Users"]
            .select {
                filter {
                    eq("UserLogin", login)
                    eq("UserPassword", password)
                }
            }
            .decodeList<User>()
            .firstOrNull()
    }

    /** Создаёт нового пользователя. Если логин занят — бросает исключение с текстом. */
    suspend fun register(login: String, password: String): User {
        val existing = SupabaseClient.client.postgrest["Users"]
            .select {
                filter { eq("UserLogin", login) }
            }
            .decodeList<User>()

        if (existing.isNotEmpty()) {
            throw IllegalStateException("Такой логин уже занят")
        }

        // UserID в базе имеет значение по умолчанию, но проще задать его самим:
        // тогда не нужно просить базу вернуть созданную строку.
        val user = User(
            id = UUID.randomUUID().toString(),
            login = login,
            password = password
        )
        SupabaseClient.client.postgrest["Users"].insert(user)
        return user
    }
}
