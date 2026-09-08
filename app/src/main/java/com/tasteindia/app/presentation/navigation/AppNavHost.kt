package com.tasteindia.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tasteindia.app.data.repository.RecipeRepository
import com.tasteindia.app.presentation.recipedetail.RecipeDetailScreen
import com.tasteindia.app.presentation.recipelist.RecipeListScreen

@Composable
fun AppNavHost(
    repository: RecipeRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.RECIPE_LIST
    ) {
        composable(NavRoutes.RECIPE_LIST) {
            RecipeListScreen(
                repository = repository,
                onRecipeClick = { mealId ->
                    navController.navigate(NavRoutes.recipeDetail(mealId))
                }
            )
        }
        composable(
            route = NavRoutes.RECIPE_DETAIL,
            arguments = listOf(
                navArgument(NavRoutes.MEAL_ID_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString(NavRoutes.MEAL_ID_ARG).orEmpty()
            RecipeDetailScreen(
                mealId = mealId,
                repository = repository,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
