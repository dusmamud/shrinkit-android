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
    const val CROP_EDITOR = "crop/{imageId}?lockedRatio={lockedRatio}&lockedLabel={lockedLabel}"
    const val SETTINGS_APPEARANCE = "settings/appearance"
    const val SETTINGS_DEFAULTS = "settings/defaults"
    const val SETTINGS_ABOUT = "settings/about"
    const val TOOLS = "tools"
    const val TOOL_DETAIL = "tools/{toolId}"
    const val RESULTS = "results"

    fun cropEditor(imageId: String): String = "crop/$imageId"

    /**
     * Crop editor with a locked aspect ratio (passport/social wizards).
     * lockedRatio = width/height float, lockedLabel = display label (e.g. "35:45").
     */
    fun cropEditorLocked(imageId: String, lockedRatio: Float, lockedLabel: String): String =
        "crop/$imageId?lockedRatio=$lockedRatio&lockedLabel=$lockedLabel"
    fun toolDetail(toolId: String): String = "tools/$toolId"
}
