package com.dusrel.shrinkit.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
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
import com.dusrel.shrinkit.presentation.settings.AboutScreen
import com.dusrel.shrinkit.presentation.settings.AppearanceScreen
import com.dusrel.shrinkit.presentation.settings.DefaultsScreen
import com.dusrel.shrinkit.presentation.tools.ToolDetailScreen
import com.dusrel.shrinkit.presentation.tools.ToolsHubScreen

private const val TRANSITION_MS = 300

/**
 * Top-level navigation: Onboarding (first run) -> Main tabs ->
 * Tool (full screen) -> CropEditor (full screen), plus the Settings
 * drill-down screens and the Tools hub/detail screens.
 *
 * Smooth screen transitions (cross-verified via Exa): direction-aware
 * fade + slide with a 300ms tween — short enough to feel instant, long
 * enough to read as motion; predictive-back compatible pop transitions.
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
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(TRANSITION_MS)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_MS)
            )
        },
        exitTransition = {
            fadeOut(tween(TRANSITION_MS)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_MS)
            )
        },
        popEnterTransition = {
            fadeIn(tween(TRANSITION_MS)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_MS)
            )
        },
        popExitTransition = {
            fadeOut(tween(TRANSITION_MS)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_MS)
            )
        }
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
                onOpenTool = { navController.navigate(Destinations.TOOL) },
                onOpenTools = { navController.navigate(Destinations.TOOLS) },
                onOpenSettingsDetail = { route -> navController.navigate(route) }
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

        composable(Destinations.SETTINGS_APPEARANCE) {
            AppearanceScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.SETTINGS_DEFAULTS) {
            DefaultsScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.SETTINGS_ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.TOOLS) {
            ToolsHubScreen(
                onBack = { navController.popBackStack() },
                onOpenTool = { toolId ->
                    navController.navigate(Destinations.toolDetail(toolId))
                }
            )
        }

        composable(
            route = Destinations.TOOL_DETAIL,
            arguments = listOf(navArgument("toolId") { type = NavType.StringType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId").orEmpty()
            ToolDetailScreen(
                toolId = toolId,
                toolViewModel = toolViewModel,
                onBack = { navController.popBackStack() },
                onOpenCropEditor = { imageId ->
                    navController.navigate(Destinations.cropEditor(imageId))
                },
                defaultQuality = prefs.defaultQuality,
                defaultFormatName = prefs.defaultFormatName
            )
        }
    }
}
