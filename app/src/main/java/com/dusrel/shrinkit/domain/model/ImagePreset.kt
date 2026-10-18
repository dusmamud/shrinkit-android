package com.dusrel.shrinkit.domain.model

data class ImagePreset(
    val id: String,
    val titleResKey: String,
    val description: String,
    val options: ProcessingOptions
) {
    companion object {
        val ALL: List<ImagePreset> = listOf(
            ImagePreset(
                id = "passport_in",
                titleResKey = "preset_passport_in",
                description = "35x45 mm, <= 50 KB JPEG",
                options = ProcessingOptions(
                    width = 3.5f,
                    height = 4.5f,
                    unit = SizeUnit.CM,
                    keepAspectRatio = false,
                    resizeMode = ResizeMode.CROP,
                    format = TargetFormat.JPEG,
                    compressMode = CompressMode.TARGET_KB,
                    targetKb = 50,
                    dpi = 300
                )
            ),
            ImagePreset(
                id = "signature_in",
                titleResKey = "preset_signature_in",
                description = "140x60 px, <= 20 KB JPEG",
                options = ProcessingOptions(
                    width = 140f,
                    height = 60f,
                    unit = SizeUnit.PIXELS,
                    keepAspectRatio = false,
                    resizeMode = ResizeMode.FIT,
                    format = TargetFormat.JPEG,
                    compressMode = CompressMode.TARGET_KB,
                    targetKb = 20
                )
            ),
            ImagePreset(
                id = "passport_us",
                titleResKey = "preset_passport_us",
                description = "2x2 in (600x600 px), <= 240 KB JPEG",
                options = ProcessingOptions(
                    width = 2f,
                    height = 2f,
                    unit = SizeUnit.INCHES,
                    keepAspectRatio = false,
                    resizeMode = ResizeMode.CROP,
                    format = TargetFormat.JPEG,
                    compressMode = CompressMode.TARGET_KB,
                    targetKb = 240,
                    dpi = 300
                )
            ),
            ImagePreset(
                id = "instagram_post",
                titleResKey = "preset_instagram_post",
                description = "1080x1080 px Square, 85% WebP",
                options = ProcessingOptions(
                    width = 1080f,
                    height = 1080f,
                    unit = SizeUnit.PIXELS,
                    keepAspectRatio = true,
                    resizeMode = ResizeMode.CROP,
                    format = TargetFormat.WEBP,
                    compressMode = CompressMode.QUALITY,
                    quality = 85
                )
            ),
            ImagePreset(
                id = "instagram_story",
                titleResKey = "preset_instagram_story",
                description = "1080x1920 px (9:16), 85% WebP",
                options = ProcessingOptions(
                    width = 1080f,
                    height = 1920f,
                    unit = SizeUnit.PIXELS,
                    keepAspectRatio = true,
                    resizeMode = ResizeMode.CROP,
                    format = TargetFormat.WEBP,
                    compressMode = CompressMode.QUALITY,
                    quality = 85
                )
            ),
            ImagePreset(
                id = "youtube_thumb",
                titleResKey = "preset_youtube_thumb",
                description = "1280x720 px, <= 2 MB JPEG",
                options = ProcessingOptions(
                    width = 1280f,
                    height = 720f,
                    unit = SizeUnit.PIXELS,
                    keepAspectRatio = true,
                    resizeMode = ResizeMode.FIT,
                    format = TargetFormat.JPEG,
                    compressMode = CompressMode.TARGET_KB,
                    targetKb = 2000
                )
            )
        )
    }
}
