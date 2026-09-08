package com.tasteindia.app.data.repository

import com.tasteindia.app.domain.model.FilterCriteria
import com.tasteindia.app.domain.model.MealDetail
import com.tasteindia.app.domain.model.MealSummary
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun getIndianMeals(forceRefresh: Boolean = false): List<MealSummary>
    suspend fun getMealDetail(mealId: String): MealDetail
    suspend fun getCategories(): List<String>
    suspend fun getIngredients(): List<String>
    suspend fun filterMeals(criteria: FilterCriteria): List<MealSummary>
    
    fun getFavouritesFlow(): Flow<Set<String>>
    fun isFavourite(mealId: String): Boolean
    fun toggleFavourite(mealId: String): Boolean
}
