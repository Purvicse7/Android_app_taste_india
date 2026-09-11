package com.tasteindia.app

import com.tasteindia.app.data.local.FavouritesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavouritesDataSourceTest {

    private class InMemoryFavouritesDataSource : FavouritesDataSource {
        private val _flow = MutableStateFlow<Set<String>>(emptySet())
        override fun getFavouritesFlow(): Flow<Set<String>> = _flow.asStateFlow()
        override fun getFavourites(): Set<String> = _flow.value
        override fun isFavourite(mealId: String): Boolean = _flow.value.contains(mealId)

        override fun toggleFavourite(mealId: String): Boolean {
            val current = _flow.value.toMutableSet()
            val isNowFav = if (current.contains(mealId)) {
                current.remove(mealId)
                false
            } else {
                current.add(mealId)
                true
            }
            _flow.value = current
            return isNowFav
        }

        override fun addFavourite(mealId: String) {
            val current = _flow.value.toMutableSet()
            current.add(mealId)
            _flow.value = current
        }

        override fun removeFavourite(mealId: String) {
            val current = _flow.value.toMutableSet()
            current.remove(mealId)
            _flow.value = current
        }
    }

    @Test
    fun toggleFavourite_addsAndRemovesMealId() {
        val dataSource = InMemoryFavouritesDataSource()
        val mealId = "52795"

        assertFalse(dataSource.isFavourite(mealId))

        // Toggle ON
        val nowFav = dataSource.toggleFavourite(mealId)
        assertTrue(nowFav)
        assertTrue(dataSource.isFavourite(mealId))
        assertEquals(setOf(mealId), dataSource.getFavourites())

        // Toggle OFF
        val removed = dataSource.toggleFavourite(mealId)
        assertFalse(removed)
        assertFalse(dataSource.isFavourite(mealId))
        assertTrue(dataSource.getFavourites().isEmpty())
    }
}
