package com.tasteindia.app.domain.model

data class MealSummary(
    val id: String,
    val name: String,
    val thumbnailUrl: String?,
    val category: String? = null
)

data class IngredientItem(
    val name: String,
    val measure: String
)

data class MealDetail(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnailUrl: String?,
    val tags: List<String>,
    val youtubeUrl: String?,
    val sourceUrl: String?,
    val ingredients: List<IngredientItem>
)

enum class SortOrder(val label: String) {
    ASCENDING("Name (A–Z)"),
    DESCENDING("Name (Z–A)")
}

data class FilterCriteria(
    val searchQuery: String = "",
    val category: String? = null,
    val ingredient: String? = null,
    val favouritesOnly: Boolean = false,
    val sortOrder: SortOrder = SortOrder.ASCENDING
) {
    val isActive: Boolean
        get() = searchQuery.isNotBlank() || category != null || ingredient != null || favouritesOnly

    fun clear(): FilterCriteria = FilterCriteria()
}
