package com.tasteindia.app

import com.tasteindia.app.core.dispatcher.CoroutineDispatchers
import com.tasteindia.app.core.network.TheMealDbApi
import com.tasteindia.app.data.dto.CategoryListResponseDto
import com.tasteindia.app.data.dto.IngredientListResponseDto
import com.tasteindia.app.data.dto.MealDetailResponseDto
import com.tasteindia.app.data.dto.MealsResponseDto
import com.tasteindia.app.data.local.FavouritesDataSource
import com.tasteindia.app.data.repository.RecipeRepositoryImpl
import com.tasteindia.app.domain.model.FilterCriteria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStreamReader

class FilterIntersectionTest {

    private class FakeMealDbApi(
        val indianMealsJson: String,
        val chickenCategoryJson: String
    ) : TheMealDbApi {
        private val json = Json { ignoreUnknownKeys = true }

        override suspend fun getIndianMeals(): MealsResponseDto =
            json.decodeFromString(indianMealsJson)

        override suspend fun getMealsByArea(area: String): MealsResponseDto =
            MealsResponseDto(emptyList())

        override suspend fun getMealDetail(mealId: String): MealDetailResponseDto =
            MealDetailResponseDto(emptyList())

        override suspend fun filterByCategory(category: String): MealsResponseDto =
            json.decodeFromString(chickenCategoryJson)

        override suspend fun filterByIngredient(ingredient: String): MealsResponseDto =
            MealsResponseDto(emptyList())

        override suspend fun getCategories(): CategoryListResponseDto =
            CategoryListResponseDto(emptyList())

        override suspend fun getIngredients(): IngredientListResponseDto =
            IngredientListResponseDto(emptyList())
    }

    private class FakeFavouritesDataSource : FavouritesDataSource {
        private val set = mutableSetOf<String>()
        override fun getFavouritesFlow(): Flow<Set<String>> = flowOf(set)
        override fun getFavourites(): Set<String> = set
        override fun isFavourite(mealId: String): Boolean = set.contains(mealId)
        override fun toggleFavourite(mealId: String): Boolean = if (set.contains(mealId)) { set.remove(mealId); false } else { set.add(mealId); true }
        override fun addFavourite(mealId: String) { set.add(mealId) }
        override fun removeFavourite(mealId: String) { set.remove(mealId) }
    }

    @Test
    fun filterMeals_preservesIndianBoundaryWhenCategoryFilterApplied() = runTest {
        val indianStream = javaClass.classLoader?.getResourceAsStream("fixtures/indian_meals.json") 
            ?: javaClass.getResourceAsStream("/fixtures/indian_meals.json")
        val chickenStream = javaClass.classLoader?.getResourceAsStream("fixtures/category_chicken.json") 
            ?: javaClass.getResourceAsStream("/fixtures/category_chicken.json")

        val indianJson = (if (indianStream != null) InputStreamReader(indianStream, Charsets.UTF_8).readText() else TestFixtures.indianMealsJson)
            .trim().removePrefix("\uFEFF")
        val chickenJson = (if (chickenStream != null) InputStreamReader(chickenStream, Charsets.UTF_8).readText() else TestFixtures.categoryChickenJson)
            .trim().removePrefix("\uFEFF")

        val fakeApi = FakeMealDbApi(indianJson, chickenJson)
        val fakeFavs = FakeFavouritesDataSource()
        val dispatchers = CoroutineDispatchers(
            main = Dispatchers.Unconfined,
            io = Dispatchers.Unconfined,
            default = Dispatchers.Unconfined
        )

        val repository = RecipeRepositoryImpl(fakeApi, fakeFavs, dispatchers)

        val criteria = FilterCriteria(category = "Chicken")
        val results = repository.filterMeals(criteria)

        val resultIds = results.map { it.id }.toSet()

        assertEquals(2, resultIds.size)
        assertTrue("Must contain Indian Chicken Handi", resultIds.contains("52795"))
        assertTrue("Must contain Indian Chicken Tikka Masala", resultIds.contains("52894"))
        assertFalse("Must NOT contain non-Indian KFC", resultIds.contains("52813"))
        assertFalse("Must NOT contain non-Indian Chicken Marengo", resultIds.contains("52920"))
    }
}
