package com.dusrel.shrinkit.presentation.navigation

/**
 * App navigation routes.
 *
 * - Onboarding: first-run intro (shown once).
 * - Main: bottom-navigation scaffold with Home / History / Settings tabs.
 * - Tool: the image resize/compress workspace (full screen).
 * - CropEditor: per-image crop editor, opened from the Tool screen.
 */
object Destinations {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val TOOL = "tool"
    const val CROP_EDITOR = "crop/{imageId}"

    fun cropEditor(imageId: String): String = "crop/$imageId"
}
