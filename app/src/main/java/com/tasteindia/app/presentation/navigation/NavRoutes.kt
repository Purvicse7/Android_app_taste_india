package com.tasteindia.app.presentation.navigation

object NavRoutes {
    const val RECIPE_LIST = "recipe_list"
    const val MEAL_ID_ARG = "mealId"
    const val RECIPE_DETAIL = "recipe_detail/{$MEAL_ID_ARG}"

    fun recipeDetail(mealId: String): String = "recipe_detail/$mealId"
}
