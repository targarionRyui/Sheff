package com.example.sheff.ui.components

import java.util.Locale


fun difficultyLabel(value: String?): String = when (value?.trim()?.lowercase()) {
    "easy", "легко", "лёгкая", "лёгкое" -> "Легко"
    "medium", "средне" -> "Средне"
    "hard", "сложно" -> "Сложно"
    else -> "—"
}

fun cookingTimeLabel(minutes: Int?): String =
    if (minutes == null || minutes <= 0) "" else "$minutes мин"


fun recipeSubtitle(cookingTime: Int?, difficulty: String?): String {
    val time = cookingTimeLabel(cookingTime)
    val level = difficultyLabel(difficulty)
    return listOf(time, level).filter { it.isNotBlank() }.joinToString(" · ")
}


fun ratingLabel(value: Double?): String? =
    value?.let { String.format(Locale.US, "%.1f", it).replace('.', ',') }


fun ingredientAmountLabel(amount: Float?, unit: String?): String {
    val number = when {
        amount == null -> null
        amount % 1f == 0f -> amount.toInt().toString()
        else -> String.format(Locale.US, "%.1f", amount).replace('.', ',')
    }
    return listOfNotNull(number, unit?.takeIf { it.isNotBlank() }).joinToString(" ")
}


fun ratingsCountLabel(count: Int): String {
    val word = when {
        count % 10 == 1 && count % 100 != 11 -> "оценка"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "оценки"
        else -> "оценок"
    }
    return "$count $word"
}


fun commentDateLabel(value: String?): String {
    if (value.isNullOrBlank()) return ""
    val datePart = value.take(10)
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else datePart
}
