package com.dusrel.shrinkit.domain.model

import android.net.Uri

data class ImageItem(
    val id: String,
    val uri: Uri,
    val filename: String,
    val originalSizeBytes: Long,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val mimeType: String = "",
    val status: ProcessingStatus = ProcessingStatus.IDLE,
    val processedUri: Uri? = null,
    val processedSizeBytes: Long = 0L,
    val processedWidth: Int = 0,
    val processedHeight: Int = 0,
    val reductionPercent: Int = 0,
    val errorMessage: String? = null
)
