package com.tasteindia.app

import android.app.Application
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tasteindia.app.core.network.TheMealDbApi
import com.tasteindia.app.data.local.FavouritesDataSource
import com.tasteindia.app.data.local.SharedPreferencesFavouritesDataSource
import com.tasteindia.app.data.repository.RecipeRepository
import com.tasteindia.app.data.repository.RecipeRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class TasteIndiaApplication : Application() {

    lateinit var recipeRepository: RecipeRepository
        private set

    lateinit var favouritesDataSource: FavouritesDataSource
        private set

    override fun onCreate() {
        super.onCreate()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(TheMealDbApi::class.java)

        favouritesDataSource = SharedPreferencesFavouritesDataSource(this)
        recipeRepository = RecipeRepositoryImpl(
            api = api,
            favouritesDataSource = favouritesDataSource
        )
    }
}
