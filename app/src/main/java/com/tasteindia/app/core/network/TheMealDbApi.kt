package com.tasteindia.app.core.network

import com.tasteindia.app.data.dto.CategoryListResponseDto
import com.tasteindia.app.data.dto.IngredientListResponseDto
import com.tasteindia.app.data.dto.MealDetailResponseDto
import com.tasteindia.app.data.dto.MealsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {

    @GET("filter.php?a=Indian")
    suspend fun getIndianMeals(): MealsResponseDto

    @GET("lookup.php")
    suspend fun getMealDetail(@Query("i") mealId: String): MealDetailResponseDto

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealsResponseDto

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealsResponseDto

    @GET("list.php?c=list")
    suspend fun getCategories(): CategoryListResponseDto

    @GET("list.php?i=list")
    suspend fun getIngredients(): IngredientListResponseDto
}
