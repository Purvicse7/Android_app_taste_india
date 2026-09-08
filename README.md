# TasteIndia — Android (Indian Cuisine Discovery)

A native Android application built with Kotlin, Jetpack Compose, and Material 3 for discovering Indian recipes powered by TheMealDB API.

---

## 1. Toolchain & Environment
- **Platform:** Android (minSdk: 26, targetSdk: 34, compileSdk: 34)
- **Language:** Kotlin 2.0.20
- **UI Framework:** Jetpack Compose (BOM 2024.09.00) & Material 3
- **Networking:** Retrofit 2.11.0 + OkHttp 4.12.0 + Kotlinx Serialization 1.6.3
- **Image Loading:** Coil 2.7.0 (Compose)
- **Testing:** JUnit 4 + Kotlinx Coroutines Test (100% offline with bundled JSON fixtures)

---

## 2. Architecture & Design

The application follows Clean Architecture with unidirectional data flow and MVI-inspired UI state machines:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│  - Jetpack Compose Screens (RecipeList, RecipeDetail)  │
│  - Lifecycle-aware ViewModels (StateFlow)              │
│  - Navigation Compose (Navigation graph & arguments)   │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                      Domain Layer                      │
│  - Clean Models (MealSummary, MealDetail, Ingredient)  │
│  - Domain Mappers (20 ingredient/measure normalization)│
│  - FilterCriteria & SortOrder                          │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                       Data Layer                       │
│  - RecipeRepositoryImpl (in-memory caching & filters)  │
│  - TheMealDbApi (Retrofit interface)                   │
│  - SharedPreferencesFavouritesDataSource (persistence) │
│  - CoroutineDispatchers (IO / Main / Default)          │
└────────────────────────────────────────────────────────┘
```

---

## 3. Key Technical Decisions & Rubric Compliance

### A. The "Indian Boundary" Local Set Intersection Strategy
TheMealDB V1 does not offer an endpoint combining cuisine area, category, and ingredients simultaneously. Calling `filter.php?c=Chicken` returns worldwide dishes (e.g., KFC, Chicken Marengo).
* **Implementation:** The app fetches the authoritative Indian meal base set via `filter.php?a=Indian`. When category or ingredient filters are applied, the returned global meal IDs are intersected locally:
  $$\text{Target IDs} = \text{Indian IDs} \cap \text{Category IDs} \cap \text{Ingredient IDs}$$
* This guarantees that non-Indian meals never leak into the discovery list.

### B. Prevention of N+1 Network Lookups & Concurrency Bounding
* The list view renders only lightweight meal summary data (`idMeal`, `strMeal`, `strMealThumb`).
* No network requests are initiated inside composable bodies during list scroll.
* Details are loaded on demand and cached in memory inside `RecipeRepositoryImpl` (`ConcurrentHashMap<String, MealDetail>`).
* In-flight detail requests are deduplicated using `ConcurrentHashMap<String, Deferred<MealDetail>>` to prevent duplicate parallel fetches for the same recipe.

### C. Ingredient 1..20 Normalization
* TheMealDB provides 40 sparse fields (`strIngredient1...20`, `strMeasure1...20`).
* `MealDetailMapper.normalizeIngredients` pairs each index, strips nulls, empty strings (`""`), and whitespace-only strings (`"   "`), outputting an ordered list of `IngredientItem`.

### D. Navigation & State Preservation
* Navigation routes pass only the stable `mealId` string (`recipe_detail/{mealId}`), not large serialized models.
* State is preserved across back stack transitions because ViewModel state lives in the navigation backstack scope.

### E. Offline Resilience & Favourites
* Favourites are stored locally via `SharedPreferencesFavouritesDataSource` (reactive `StateFlow`) and load immediately on cold launch without network access.
* Network failures present an explicit `ErrorRetryView` with a manual "Retry" action.
* Coil handles async image loading with a placeholder and error fallback without causing layout collapse.

---

## 4. Route Map
- `recipe_list`: Main discovery screen with search, active filters banner, sort controls, and favourites toggle.
- `recipe_detail/{mealId}`: Recipe detail screen displaying the hero image, badges, tags, ingredients table, instructions, and external link buttons.

---

## 5. Offline Unit Tests

Run the test suite via Gradle:
```bash
./gradlew test
```

The test suite runs 100% offline using bundled JSON fixtures in `app/src/test/resources/fixtures/`:
1. `MealDetailMapperTest`: Validates normalization of 20 sparse ingredient/measure pairs and omission of whitespace/empty entries.
2. `FilterIntersectionTest`: Verifies that category filtering preserves the Indian boundary and excludes non-Indian items.
3. `RecipeListViewModelTest`: Tests search input debounce (300ms) and query filtering.
4. `FavouritesDataSourceTest`: Tests adding, removing, and toggling persisted favourite meal IDs offline.

---

## 6. Assumptions & Tradeoffs
- **Local Search:** Given the size of the Indian collection on TheMealDB (~30-40 meals), performing name filtering locally across the loaded Indian set ensures sub-millisecond response times, preserves the Indian boundary, and avoids unnecessary network bandwidth.
- **In-Memory Detail Cache:** Caching viewed details in memory provides instant re-navigation without disk overhead.

---

## 7. AI Tool Disclosure
- AI assistance was used for generating boilerplate DTO structures, JSON mock fixtures, and initial test setup.
- All domain mappings, set intersection logic, concurrency controls, and Jetpack Compose UI components were reviewed, adapted, and verified manually.
