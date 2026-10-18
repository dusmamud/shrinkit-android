package com.dusrel.shrinkit.domain.model

enum class SizeUnit(val id: String, val suffix: String, val min: Float, val max: Float) {
    PERCENT("percent", "%", 1f, 500f),
    PIXELS("pixels", "px", 1f, 12000f),
    CM("cm", "cm", 0.1f, 400f),
    INCHES("inches", "in", 0.1f, 160f);

    companion object {
        fun fromId(id: String): SizeUnit = entries.find { it.id == id } ?: PERCENT
    }
}

enum class ResizeMode(val id: String) {
    STRETCH("stretch"),
    CROP("crop"),
    FIT("fit")
}

enum class TargetFormat(val extension: String, val mimeType: String, val supportsAlpha: Boolean) {
    ORIGINAL("original", "", true),
    JPEG("jpg", "image/jpeg", false),
    PNG("png", "image/png", true),
    WEBP("webp", "image/webp", true),
    AVIF("avif", "image/avif", true)
}

enum class CompressMode {
    QUALITY,
    TARGET_KB
}

enum class BackgroundColor(val argb: Int) {
    WHITE(0xFFFFFFFF.toInt()),
    BLACK(0xFF000000.toInt())
}

enum class ProcessingStatus {
    IDLE,
    QUEUED,
    PROCESSING,
    COMPLETED,
    ERROR
}
