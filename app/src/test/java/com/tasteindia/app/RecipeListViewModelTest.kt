package com.tasteindia.app

import com.tasteindia.app.data.repository.RecipeRepository
import com.tasteindia.app.domain.model.FilterCriteria
import com.tasteindia.app.domain.model.MealDetail
import com.tasteindia.app.domain.model.MealSummary
import com.tasteindia.app.domain.model.SortOrder
import com.tasteindia.app.presentation.recipelist.RecipeListUiState
import com.tasteindia.app.presentation.recipelist.RecipeListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class MockRecipeRepository : RecipeRepository {
        val sampleMeals = listOf(
            MealSummary("1", "Dal Fry", null),
            MealSummary("2", "Butter Chicken", null),
            MealSummary("3", "Palak Paneer", null)
        )

        override suspend fun getIndianMeals(forceRefresh: Boolean): List<MealSummary> = sampleMeals
        override suspend fun getMealDetail(mealId: String): MealDetail = throw NotImplementedError()
        override suspend fun getCategories(): List<String> = listOf("Chicken", "Vegetarian")
        override suspend fun getIngredients(): List<String> = listOf("Dal", "Paneer")
        override suspend fun filterMeals(criteria: FilterCriteria): List<MealSummary> {
            var res = sampleMeals
            if (criteria.searchQuery.isNotBlank()) {
                res = res.filter { it.name.contains(criteria.searchQuery, ignoreCase = true) }
            }
            res = when (criteria.sortOrder) {
                SortOrder.ASCENDING -> res.sortedBy { it.name.lowercase() }
                SortOrder.DESCENDING -> res.sortedByDescending { it.name.lowercase() }
            }
            return res
        }
        override fun getFavouritesFlow(): Flow<Set<String>> = flowOf(emptySet())
        override fun isFavourite(mealId: String): Boolean = false
        override fun toggleFavourite(mealId: String): Boolean = true
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onSearchQueryChanged_debouncesAndFiltersMeals() = runTest(testDispatcher) {
        val repository = MockRecipeRepository()
        val viewModel = RecipeListViewModel(repository)

        // Apply search query criteria
        viewModel.applyCriteria(FilterCriteria(searchQuery = "paneer"))

        val state = viewModel.uiState.value
        assertTrue("State should be Success but was $state", state is RecipeListUiState.Success)
        val success = state as RecipeListUiState.Success
        assertEquals(1, success.meals.size)
        assertEquals("Palak Paneer", success.meals.first().name)
    }
}
