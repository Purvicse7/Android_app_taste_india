package com.tasteindia.app.presentation.recipelist

import com.tasteindia.app.domain.model.MealSummary

sealed interface RecipeListUiState {
    data object Loading : RecipeListUiState
    data class Success(
        val meals: List<MealSummary>,
        val favouriteIds: Set<String>,
        val totalCount: Int
    ) : RecipeListUiState
    data class Empty(val message: String) : RecipeListUiState
    data class Error(val message: String, val canRetry: Boolean = true) : RecipeListUiState
}
