package com.tasteindia.app.presentation.recipedetail

import com.tasteindia.app.domain.model.MealDetail

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState
    data class Success(val detail: MealDetail, val isFavourite: Boolean) : RecipeDetailUiState
    data class Error(val message: String) : RecipeDetailUiState
}
