package com.tasteindia.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface FavouritesDataSource {
    fun getFavouritesFlow(): Flow<Set<String>>
    fun getFavourites(): Set<String>
    fun isFavourite(mealId: String): Boolean
    fun toggleFavourite(mealId: String): Boolean
    fun addFavourite(mealId: String)
    fun removeFavourite(mealId: String)
}

class SharedPreferencesFavouritesDataSource(
    context: Context,
    prefsName: String = "taste_india_favourites_prefs"
) : FavouritesDataSource {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val key = "favourite_meal_ids"
    private val _favouritesFlow = MutableStateFlow<Set<String>>(loadStoredFavourites())

    override fun getFavouritesFlow(): Flow<Set<String>> = _favouritesFlow.asStateFlow()

    override fun getFavourites(): Set<String> = _favouritesFlow.value

    override fun isFavourite(mealId: String): Boolean = _favouritesFlow.value.contains(mealId)

    @Synchronized
    override fun toggleFavourite(mealId: String): Boolean {
        val current = _favouritesFlow.value.toMutableSet()
        val isNowFav = if (current.contains(mealId)) {
            current.remove(mealId)
            false
        } else {
            current.add(mealId)
            true
        }
        persist(current)
        _favouritesFlow.value = current
        return isNowFav
    }

    @Synchronized
    override fun addFavourite(mealId: String) {
        val current = _favouritesFlow.value.toMutableSet()
        if (current.add(mealId)) {
            persist(current)
            _favouritesFlow.value = current
        }
    }

    @Synchronized
    override fun removeFavourite(mealId: String) {
        val current = _favouritesFlow.value.toMutableSet()
        if (current.remove(mealId)) {
            persist(current)
            _favouritesFlow.value = current
        }
    }

    private fun loadStoredFavourites(): Set<String> {
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun persist(ids: Set<String>) {
        prefs.edit().putStringSet(key, ids).apply()
    }
}
