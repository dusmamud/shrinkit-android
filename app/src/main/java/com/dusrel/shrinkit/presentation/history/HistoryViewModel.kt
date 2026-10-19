package com.dusrel.shrinkit.presentation.history

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dusrel.shrinkit.data.local.HistoryDao
import com.dusrel.shrinkit.data.local.ProcessedImageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val application: Application,
    private val historyDao: HistoryDao
) : AndroidViewModel(application) {

    val history = historyDao.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun delete(entity: ProcessedImageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Best effort: remove the MediaStore entry too.
                runCatching {
                    application.contentResolver.delete(Uri.parse(entity.contentUri), null, null)
                }
                historyDao.deleteById(entity.id)
            } catch (_: Exception) {
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyDao.clearAll()
            } catch (_: Exception) {
            }
        }
    }

    /** Shares the processed file via the system share sheet. */
    fun share(entity: ProcessedImageEntity) {
        try {
            val uri = Uri.parse(entity.contentUri)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            application.startActivity(chooser)
        } catch (_: Exception) {
        }
    }
}
