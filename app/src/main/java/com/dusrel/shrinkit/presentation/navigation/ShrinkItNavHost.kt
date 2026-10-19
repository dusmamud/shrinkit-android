package com.dusrel.shrinkit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dusrel.shrinkit.data.local.UserPreferences
import com.dusrel.shrinkit.presentation.ShrinkItApp
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.crop.CropEditorScreen
import com.dusrel.shrinkit.presentation.main.MainScreen
import com.dusrel.shrinkit.presentation.onboarding.OnboardingScreen

/**
 * Top-level navigation: Onboarding (first run) -> Main tabs ->
 * Tool (full screen) -> CropEditor (full screen).
 *
 * The [toolViewModel] is activity-scoped so Home (camera capture) and the
 * Tool share the same image session.
 */
@Composable
fun ShrinkItNavHost(
    navController: NavHostController,
    toolViewModel: ShrinkItViewModel,
    prefs: UserPreferences,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (prefs.onboardingDone) Destinations.MAIN else Destinations.ONBOARDING,
        modifier = modifier
    ) {
        composable(Destinations.ONBOARDING) {
            OnboardingScreen(onFinished = {
                onOnboardingFinished()
                navController.navigate(Destinations.MAIN) {
                    popUpTo(Destinations.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Destinations.MAIN) {
            MainScreen(
                toolViewModel = toolViewModel,
                onOpenTool = { navController.navigate(Destinations.TOOL) }
            )
        }

        composable(Destinations.TOOL) {
            ShrinkItApp(
                viewModel = toolViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenCropEditor = { imageId ->
                    navController.navigate(Destinations.cropEditor(imageId))
                },
                defaultQuality = prefs.defaultQuality,
                defaultFormatName = prefs.defaultFormatName
            )
        }

        composable(
            route = Destinations.CROP_EDITOR,
            arguments = listOf(navArgument("imageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getString("imageId").orEmpty()
            CropEditorScreen(
                imageId = imageId,
                toolViewModel = toolViewModel,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
