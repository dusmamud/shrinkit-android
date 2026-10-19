package com.dusrel.shrinkit.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dusrel.shrinkit.data.local.ThemeMode
import com.dusrel.shrinkit.data.local.UserPreferencesRepository
import com.dusrel.shrinkit.presentation.navigation.ShrinkItNavHost
import com.dusrel.shrinkit.presentation.theme.ShrinkItTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val scope = rememberCoroutineScope()
            // Null until DataStore emits: avoids picking the wrong start destination.
            val prefs by prefsRepository.preferences.collectAsState(initial = null)
            // Activity-scoped: shared between Home (camera) and the Tool.
            val toolViewModel: ShrinkItViewModel = hiltViewModel()

            val darkTheme = when (prefs?.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }

            ShrinkItTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loadedPrefs = prefs
                    if (loadedPrefs == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        ShrinkItNavHost(
                            navController = navController,
                            toolViewModel = toolViewModel,
                            prefs = loadedPrefs,
                            onOnboardingFinished = {
                                scope.launch {
                                    prefsRepository.setOnboardingDone()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
