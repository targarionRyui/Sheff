package com.example.sheff.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sheff.data.models.Category
import com.example.sheff.data.models.Ingredient
import com.example.sheff.ui.theme.BrownText
import com.example.sheff.ui.theme.CardGray
import com.example.sheff.ui.theme.Cream
import com.example.sheff.ui.theme.InkBlack
import com.example.sheff.ui.theme.Yellow
import com.example.sheff.viewmodel.CatalogFilters

private val difficultyOptions = listOf(
    "easy" to "Легко",
    "medium" to "Средне",
    "hard" to "Сложно"
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogFilterSheet(
    filters: CatalogFilters,
    categories: List<Category>,
    ingredients: List<Ingredient>,
    onDismiss: () -> Unit,
    onApply: (CatalogFilters) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var draft by remember { mutableStateOf(filters) }
    var timeFromText by remember { mutableStateOf(filters.timeFrom?.toString().orEmpty()) }
    var timeToText by remember { mutableStateOf(filters.timeTo?.toString().orEmpty()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Фильтры",
                    style = MaterialTheme.typography.titleLarge,
                    color = BrownText,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        draft = CatalogFilters()
                        timeFromText = ""
                        timeToText = ""
                    }
                ) {
                    Text("Сбросить", color = BrownText)
                }
            }



            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---------- Ингредиенты ----------
                FilterSectionTitle("Ингредиенты")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ingredients.forEach { ingredient ->
                        val selected = ingredient.id in draft.ingredientIds
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val newIds = if (selected) {
                                    draft.ingredientIds - ingredient.id
                                } else {
                                    draft.ingredientIds + ingredient.id
                                }
                                draft = draft.copy(ingredientIds = newIds)
                            },
                            label = { Text(ingredient.name) },
                            colors = filterChipColors()
                        )
                    }
                }

                if (draft.ingredientIds.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = draft.strictIngredients,
                            onCheckedChange = { draft = draft.copy(strictIngredients = it) }
                        )
                        Text(
                            text = "Только если есть все выбранные",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrownText
                        )
                    }
                }

                // ---------- Сложность ----------
                FilterSectionTitle("Сложность")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyOptions.forEach { (value, label) ->
                        val selected = value in draft.difficulties
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val newSet = if (selected) {
                                    draft.difficulties - value
                                } else {
                                    draft.difficulties + value
                                }
                                draft = draft.copy(difficulties = newSet)
                            },
                            label = { Text(label) },
                            colors = filterChipColors()
                        )
                    }
                }

                // ---------- Категория ----------
                FilterSectionTitle("Категория")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        val selected = draft.categoryId == category.id
                        FilterChip(
                            selected = selected,
                            onClick = {
                                draft = draft.copy(categoryId = if (selected) null else category.id)
                            },
                            label = { Text(category.name) },
                            colors = filterChipColors()
                        )
                    }
                }

                // ---------- Время ----------
                FilterSectionTitle("Время готовки, мин")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = timeFromText,
                        onValueChange = { timeFromText = it.filter(Char::isDigit).take(3) },
                        label = { Text("от") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeToText,
                        onValueChange = { timeToText = it.filter(Char::isDigit).take(3) },
                        label = { Text("до") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onApply(
                        draft.copy(
                            timeFrom = timeFromText.toIntOrNull(),
                            timeTo = timeToText.toIntOrNull()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow,
                    contentColor = InkBlack
                )
            ) {
                Text("Применить", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(text: String) {
    Spacer(Modifier.height(18.dp))
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = BrownText
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = CardGray.copy(alpha = 0.45f),
    labelColor = BrownText,
    selectedContainerColor = Yellow,
    selectedLabelColor = InkBlack
)
