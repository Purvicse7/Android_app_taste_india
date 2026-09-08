package com.tasteindia.app.presentation.recipelist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasteindia.app.domain.model.FilterCriteria
import com.tasteindia.app.domain.model.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentCriteria: FilterCriteria,
    availableCategories: List<String>,
    availableIngredients: List<String>,
    onDismiss: () -> Unit,
    onApply: (FilterCriteria) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftCriteria by remember { mutableStateOf(currentCriteria) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Filter & Sort Recipes",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sort Order
            Text(text = "Sort By", style = MaterialTheme.typography.titleMedium)
            SortOrder.entries.forEach { order ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { draftCriteria = draftCriteria.copy(sortOrder = order) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = draftCriteria.sortOrder == order,
                        onClick = { draftCriteria = draftCriteria.copy(sortOrder = order) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = order.label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Favourites Only
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Favourites Only",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = draftCriteria.favouritesOnly,
                    onCheckedChange = { draftCriteria = draftCriteria.copy(favouritesOnly = it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Category Filter
            Text(text = "Category Filter", style = MaterialTheme.typography.titleMedium)
            Text(
                text = draftCriteria.category ?: "All Categories",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { draftCriteria = draftCriteria.copy(category = null) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Clear Category")
                }
                if (availableCategories.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val next = availableCategories.firstOrNull { it != draftCriteria.category }
                            draftCriteria = draftCriteria.copy(category = next)
                        }
                    ) {
                        Text("Cycle Category")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        draftCriteria = FilterCriteria()
                        onApply(draftCriteria)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset All")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        onApply(draftCriteria)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
