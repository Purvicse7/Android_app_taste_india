package com.tasteindia.app.presentation.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasteindia.app.data.repository.RecipeRepository
import com.tasteindia.app.domain.model.FilterCriteria
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class RecipeListViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeListUiState>(RecipeListUiState.Loading)
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private val _criteria = MutableStateFlow(FilterCriteria())
    val criteria: StateFlow<FilterCriteria> = _criteria.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _availableIngredients = MutableStateFlow<List<String>>(emptyList())
    val availableIngredients: StateFlow<List<String>> = _availableIngredients.asStateFlow()

    init {
        loadFilterMetadata()
        loadRecipes()

        // Debounce search input to prevent rapid stale requests
        _searchQuery
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                _criteria.value = _criteria.value.copy(searchQuery = query)
                loadRecipes()
            }
            .launchIn(viewModelScope)

        // Reactively update favourites
        repository.getFavouritesFlow()
            .onEach { favs ->
                val current = _uiState.value
                if (current is RecipeListUiState.Success) {
                    _uiState.value = current.copy(favouriteIds = favs)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun applyCriteria(newCriteria: FilterCriteria) {
        _criteria.value = newCriteria
        _searchQuery.value = newCriteria.searchQuery
        loadRecipes()
    }

    fun clearFilters() {
        val cleared = FilterCriteria()
        _criteria.value = cleared
        _searchQuery.value = ""
        loadRecipes()
    }

    fun toggleFavourite(mealId: String) {
        repository.toggleFavourite(mealId)
        if (_criteria.value.favouritesOnly) {
            loadRecipes()
        }
    }

    fun retry() {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = RecipeListUiState.Loading
            try {
                val meals = repository.filterMeals(_criteria.value)
                val favs = repository.getFavouritesFlow()
                if (meals.isEmpty()) {
                    _uiState.value = RecipeListUiState.Empty(
                        "No Indian recipes match your selected filters and search query."
                    )
                } else {
                    val currentFavs = repository.getFavouritesFlow()
                    _uiState.value = RecipeListUiState.Success(
                        meals = meals,
                        favouriteIds = emptySet(),
                        totalCount = meals.size
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RecipeListUiState.Error(
                    message = e.localizedMessage ?: "Failed to load Indian recipes."
                )
            }
        }
    }

    private fun loadFilterMetadata() {
        viewModelScope.launch {
            runCatching {
                _availableCategories.value = repository.getCategories()
                _availableIngredients.value = repository.getIngredients()
            }
        }
    }

    companion object {
        fun provideFactory(repository: RecipeRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecipeListViewModel(repository) as T
                }
            }
    }
}
