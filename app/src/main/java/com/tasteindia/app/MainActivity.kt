package com.tasteindia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasteindia.app.presentation.navigation.AppNavHost
import com.tasteindia.app.presentation.theme.TasteIndiaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TasteIndiaApplication
        val repository = app.recipeRepository

        setContent {
            TasteIndiaTheme {
                AppNavHost(repository = repository)
            }
        }
    }
}
