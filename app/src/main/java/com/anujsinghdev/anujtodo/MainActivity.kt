package com.anujsinghdev.anujtodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // 1. Import this
import androidx.navigation.compose.rememberNavController
import com.anujsinghdev.anujtodo.ui.Navigation
import com.anujsinghdev.anujtodo.ui.theme.AnujToDoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 2. Install Splash Screen (Must be BEFORE super.onCreate)
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // 3. Keep the Splash Screen visible while "isLoading" is true
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        enableEdgeToEdge()
        setContent {
            AnujToDoTheme {
                // 4. Only show content when loading is finished.
                // We removed the CircularProgressIndicator because the
                // splash screen now covers the loading time!
                if (!viewModel.isLoading.value) {
                    val navController = rememberNavController()
                    val startScreen = viewModel.startDestination.value

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            Navigation(
                                navController = navController,
                                startDestination = startScreen
                            )
                        }
                    }
                } else {
                    // While loading, just render a blank box behind the splash screen
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}