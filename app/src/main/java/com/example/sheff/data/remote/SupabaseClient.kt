package com.example.sheff.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    private const val SUPABASE_URL = "https://xwcuxkeksshecdorvhlr.supabase.co"

    // Вставь сюда свой anon ключ
    private const val SUPABASE_KEY = "sb_publishable_eqpJA19ZWHuUaBZMMU50Yw_gzf6mpXK"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest) // Модуль для работы с таблицами (базой данных)
    }
}