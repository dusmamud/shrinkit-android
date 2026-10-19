package com.dusrel.shrinkit.presentation.history

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.data.local.HistoryDao
import com.dusrel.shrinkit.data.local.ProcessedImageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun delete(entity: ProcessedImageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Best effort: remove the MediaStore entry too.
                runCatching {
                    application.contentResolver.delete(Uri.parse(entity.contentUri), null, null)
                }
                historyDao.deleteById(entity.id)
                _userMessage.value = application.getString(R.string.history_deleted)
            } catch (_: Exception) {
                _userMessage.value = application.getString(R.string.history_delete_failed)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyDao.clearAll()
            } catch (_: Exception) {
                _userMessage.value = application.getString(R.string.history_delete_failed)
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
            _userMessage.value = application.getString(R.string.history_share_failed)
        }
    }
}
