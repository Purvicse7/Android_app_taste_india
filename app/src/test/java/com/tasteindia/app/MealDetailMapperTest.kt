package com.tasteindia.app

import com.tasteindia.app.data.dto.MealDetailResponseDto
import com.tasteindia.app.domain.mapper.MealDetailMapper
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.InputStreamReader

class MealDetailMapperTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun normalizeIngredients_omitsBlankAndWhitespaceEntries() {
        val stream = javaClass.classLoader?.getResourceAsStream("fixtures/meal_lookup_52772.json")
        assertNotNull("Fixture meal_lookup_52772.json must exist", stream)

        val jsonString = InputStreamReader(stream!!).readText()
        val response = json.decodeFromString<MealDetailResponseDto>(jsonString)
        val dto = response.meals?.firstOrNull()
        assertNotNull("DTO should not be null", dto)

        val detail = MealDetailMapper.mapDetail(dto!!)

        // Expected 6 valid ingredient/measure rows
        assertEquals(6, detail.ingredients.size)
        assertEquals("Chicken", detail.ingredients[0].name)
        assertEquals("1.2 kg", detail.ingredients[0].measure)
        assertEquals("Coriander Leaves", detail.ingredients[5].name)
        assertEquals("A handful", detail.ingredients[5].measure)

        // Ensure empty string and whitespace entries were excluded
        val ingredientNames = detail.ingredients.map { it.name }
        assertFalse(ingredientNames.contains(""))
        assertFalse(ingredientNames.contains("   "))

        // Tags parsing
        assertEquals(listOf("Curry", "Spicy", "Dinner"), detail.tags)
    }
}
