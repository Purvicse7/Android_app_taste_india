package com.tasteindia.app.presentation.recipedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasteindia.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val mealId: String,
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = RecipeDetailUiState.Loading
            try {
                val detail = repository.getMealDetail(mealId)
                val isFav = repository.isFavourite(mealId)
                _uiState.value = RecipeDetailUiState.Success(detail = detail, isFavourite = isFav)
            } catch (e: Exception) {
                _uiState.value = RecipeDetailUiState.Error(
                    e.localizedMessage ?: "Failed to load meal details."
                )
            }
        }
    }

    fun toggleFavourite() {
        val current = _uiState.value
        if (current is RecipeDetailUiState.Success) {
            val isNowFav = repository.toggleFavourite(current.detail.id)
            _uiState.value = current.copy(isFavourite = isNowFav)
        }
    }

    companion object {
        fun provideFactory(mealId: String, repository: RecipeRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecipeDetailViewModel(mealId, repository) as T
                }
            }
    }
}
