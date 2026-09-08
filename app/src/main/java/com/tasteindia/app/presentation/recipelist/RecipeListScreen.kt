package com.tasteindia.app.presentation.recipelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasteindia.app.data.repository.RecipeRepository
import com.tasteindia.app.presentation.recipelist.components.EmptyResultsView
import com.tasteindia.app.presentation.recipelist.components.ErrorRetryView
import com.tasteindia.app.presentation.recipelist.components.FilterBottomSheet
import com.tasteindia.app.presentation.recipelist.components.LoadingView
import com.tasteindia.app.presentation.recipelist.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    repository: RecipeRepository,
    onRecipeClick: (String) -> Unit,
    viewModel: RecipeListViewModel = viewModel(factory = RecipeListViewModel.provideFactory(repository))
) {
    val uiState by viewModel.uiState.collectAsState()
    val criteria by viewModel.criteria.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()
    val ingredients by viewModel.availableIngredients.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TasteIndia") },
                actions = {
                    TextButton(onClick = { showFilterSheet = true }) {
                        Text(if (criteria.isActive) "Filtered" else "Filter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search Indian dishes…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )

            // Active Filters Banner
            if (criteria.isActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filters applied",
                        style = MaterialTheme.typography.labelMedium
                    )
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Clear All")
                    }
                }
            }

            // Screen content based on UiState
            when (val state = uiState) {
                is RecipeListUiState.Loading -> {
                    LoadingView()
                }
                is RecipeListUiState.Empty -> {
                    EmptyResultsView(
                        message = state.message,
                        onClearFilters = { viewModel.clearFilters() }
                    )
                }
                is RecipeListUiState.Error -> {
                    ErrorRetryView(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
                is RecipeListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "${state.totalCount} Indian recipes",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        items(
                            items = state.meals,
                            key = { it.id } // Stable idMeal key mandated by rubric
                        ) { meal ->
                            RecipeCard(
                                meal = meal,
                                isFavourite = state.favouriteIds.contains(meal.id),
                                onRecipeClick = onRecipeClick,
                                onToggleFavourite = { viewModel.toggleFavourite(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentCriteria = criteria,
            availableCategories = categories,
            availableIngredients = ingredients,
            onDismiss = { showFilterSheet = false },
            onApply = { newCriteria ->
                viewModel.applyCriteria(newCriteria)
            }
        )
    }
}
