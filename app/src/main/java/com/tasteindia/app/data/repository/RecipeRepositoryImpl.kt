package com.tasteindia.app.data.repository

import com.tasteindia.app.core.dispatcher.CoroutineDispatchers
import com.tasteindia.app.core.network.TheMealDbApi
import com.tasteindia.app.data.local.FavouritesDataSource
import com.tasteindia.app.domain.mapper.MealDetailMapper
import com.tasteindia.app.domain.model.FilterCriteria
import com.tasteindia.app.domain.model.MealDetail
import com.tasteindia.app.domain.model.MealSummary
import com.tasteindia.app.domain.model.SortOrder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class RecipeRepositoryImpl(
    private val api: TheMealDbApi,
    private val favouritesDataSource: FavouritesDataSource,
    private val dispatchers: CoroutineDispatchers = CoroutineDispatchers()
) : RecipeRepository {

    // In-memory cache for the Indian base set and looked-up meal details to prevent N+1 queries
    private var cachedIndianMeals: List<MealSummary>? = null
    private val detailCache = ConcurrentHashMap<String, MealDetail>()
    private val inFlightDetailRequests = ConcurrentHashMap<String, Deferred<MealDetail>>()
    private val cacheMutex = Mutex()

    private var cachedCategories: List<String>? = null
    private var cachedIngredients: List<String>? = null

    override suspend fun getIndianMeals(forceRefresh: Boolean): List<MealSummary> = withContext(dispatchers.io) {
        if (!forceRefresh) {
            cachedIndianMeals?.let { return@withContext it }
        }
        cacheMutex.withLock {
            if (!forceRefresh && cachedIndianMeals != null) {
                return@withLock cachedIndianMeals!!
            }
            var response = runCatching { api.getIndianMeals() }.getOrNull()
            var meals = response?.meals.orEmpty()
            if (meals.isEmpty()) {
                val fallback = runCatching { api.getMealsByArea("Indian") }.getOrNull()
                val fallbackMeals = fallback?.meals.orEmpty()
                if (fallbackMeals.isNotEmpty()) {
                    meals = fallbackMeals
                }
            }
            val summaries = meals.map { MealDetailMapper.mapSummary(it) }
            cachedIndianMeals = summaries
            summaries
        }
    }

    override suspend fun getMealDetail(mealId: String): MealDetail = withContext(dispatchers.io) {
        detailCache[mealId]?.let { return@withContext it }

        coroutineScope {
            val deferred = inFlightDetailRequests.computeIfAbsent(mealId) {
                async {
                    val response = api.getMealDetail(mealId)
                    val dto = response.meals?.firstOrNull()
                        ?: throw IllegalStateException("No detail found for meal id: $mealId")
                    val detail = MealDetailMapper.mapDetail(dto)
                    detailCache[mealId] = detail
                    detail
                }
            }

            try {
                deferred.await()
            } finally {
                inFlightDetailRequests.remove(mealId)
            }
        }
    }

    override suspend fun getCategories(): List<String> = withContext(dispatchers.io) {
        cachedCategories?.let { return@withContext it }
        val response = api.getCategories()
        val list = response.meals.orEmpty().map { it.strCategory }.distinct().sorted()
        cachedCategories = list
        list
    }

    override suspend fun getIngredients(): List<String> = withContext(dispatchers.io) {
        cachedIngredients?.let { return@withContext it }
        val response = api.getIngredients()
        val list = response.meals.orEmpty().map { it.strIngredient }.distinct().sorted()
        cachedIngredients = list
        list
    }

    override suspend fun filterMeals(criteria: FilterCriteria): List<MealSummary> = withContext(dispatchers.io) {
        // Step 1: Ensure Indian base collection is authoritative
        val baseIndianMeals = getIndianMeals()
        val indianIdSet = baseIndianMeals.map { it.id }.toSet()
        var candidateIds = indianIdSet

        // Step 2: Category filter with local Set Intersection
        if (!criteria.category.isNullOrBlank()) {
            val categoryResponse = api.filterByCategory(criteria.category)
            val categoryIds = categoryResponse.meals.orEmpty().map { it.idMeal }.toSet()
            candidateIds = candidateIds.intersect(categoryIds)
        }

        // Step 3: Ingredient filter with local Set Intersection
        if (!criteria.ingredient.isNullOrBlank()) {
            val ingredientResponse = api.filterByIngredient(criteria.ingredient)
            val ingredientIds = ingredientResponse.meals.orEmpty().map { it.idMeal }.toSet()
            candidateIds = candidateIds.intersect(ingredientIds)
        }

        // Step 4: Favourites-only filter
        if (criteria.favouritesOnly) {
            val favIds = favouritesDataSource.getFavourites()
            candidateIds = candidateIds.intersect(favIds)
        }

        // Step 5: Filter master Indian meal collection by candidate IDs
        var filteredMeals = baseIndianMeals.filter { candidateIds.contains(it.id) }

        // Step 6: Local text search query
        val query = criteria.searchQuery.trim()
        if (query.isNotBlank()) {
            filteredMeals = filteredMeals.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        // Step 7: Apply sort order
        filteredMeals = when (criteria.sortOrder) {
            SortOrder.ASCENDING -> filteredMeals.sortedBy { it.name.lowercase() }
            SortOrder.DESCENDING -> filteredMeals.sortedByDescending { it.name.lowercase() }
        }

        filteredMeals
    }

    override fun getFavouritesFlow(): Flow<Set<String>> = favouritesDataSource.getFavouritesFlow()

    override fun isFavourite(mealId: String): Boolean = favouritesDataSource.isFavourite(mealId)

    override fun toggleFavourite(mealId: String): Boolean = favouritesDataSource.toggleFavourite(mealId)
}
