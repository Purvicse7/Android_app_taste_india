package com.tasteindia.app.domain.mapper

import com.tasteindia.app.data.dto.MealDetailDto
import com.tasteindia.app.data.dto.MealSummaryDto
import com.tasteindia.app.domain.model.IngredientItem
import com.tasteindia.app.domain.model.MealDetail
import com.tasteindia.app.domain.model.MealSummary

object MealDetailMapper {

    fun mapSummary(dto: MealSummaryDto): MealSummary {
        return MealSummary(
            id = dto.idMeal,
            name = dto.strMeal.trim(),
            thumbnailUrl = dto.strMealThumb?.trim()?.takeIf { it.isNotBlank() }
        )
    }

    fun mapDetail(dto: MealDetailDto): MealDetail {
        return MealDetail(
            id = dto.idMeal,
            name = dto.strMeal.trim(),
            category = dto.strCategory?.trim().orEmpty(),
            area = dto.strArea?.trim().orEmpty(),
            instructions = dto.strInstructions?.trim().orEmpty(),
            thumbnailUrl = sanitizeUrl(dto.strMealThumb),
            tags = parseTags(dto.strTags),
            youtubeUrl = sanitizeUrl(dto.strYoutube),
            sourceUrl = sanitizeUrl(dto.strSource),
            ingredients = normalizeIngredients(dto)
        )
    }

    fun normalizeIngredients(dto: MealDetailDto): List<IngredientItem> {
        val rawPairs = listOf(
            dto.strIngredient1 to dto.strMeasure1,
            dto.strIngredient2 to dto.strMeasure2,
            dto.strIngredient3 to dto.strMeasure3,
            dto.strIngredient4 to dto.strMeasure4,
            dto.strIngredient5 to dto.strMeasure5,
            dto.strIngredient6 to dto.strMeasure6,
            dto.strIngredient7 to dto.strMeasure7,
            dto.strIngredient8 to dto.strMeasure8,
            dto.strIngredient9 to dto.strMeasure9,
            dto.strIngredient10 to dto.strMeasure10,
            dto.strIngredient11 to dto.strMeasure11,
            dto.strIngredient12 to dto.strMeasure12,
            dto.strIngredient13 to dto.strMeasure13,
            dto.strIngredient14 to dto.strMeasure14,
            dto.strIngredient15 to dto.strMeasure15,
            dto.strIngredient16 to dto.strMeasure16,
            dto.strIngredient17 to dto.strMeasure17,
            dto.strIngredient18 to dto.strMeasure18,
            dto.strIngredient19 to dto.strMeasure19,
            dto.strIngredient20 to dto.strMeasure20
        )

        val result = mutableListOf<IngredientItem>()
        for ((rawIng, rawMeasure) in rawPairs) {
            val ing = rawIng?.trim().orEmpty()
            if (ing.isNotBlank()) {
                val measure = rawMeasure?.trim().orEmpty()
                result.add(IngredientItem(name = ing, measure = measure))
            }
        }
        return result
    }

    fun parseTags(rawTags: String?): List<String> {
        if (rawTags.isNullOrBlank()) return emptyList()
        return rawTags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun sanitizeUrl(url: String?): String? {
        val trimmed = url?.trim().orEmpty()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            null
        }
    }
}
