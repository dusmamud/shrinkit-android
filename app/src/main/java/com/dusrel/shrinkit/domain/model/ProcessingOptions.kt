package com.dusrel.shrinkit.domain.model

data class ProcessingOptions(
    val width: Float = 70f,
    val height: Float = 70f,
    val unit: SizeUnit = SizeUnit.PERCENT,
    val keepAspectRatio: Boolean = true,
    val resizeMode: ResizeMode = ResizeMode.STRETCH,
    val format: TargetFormat = TargetFormat.ORIGINAL,
    val compressMode: CompressMode = CompressMode.QUALITY,
    val quality: Int = 85,
    val targetKb: Int = 200,
    val dpi: Int = 72,
    val backgroundColor: BackgroundColor = BackgroundColor.WHITE,
    val stripExif: Boolean = true
)
