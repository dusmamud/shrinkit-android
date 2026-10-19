package com.dusrel.shrinkit.presentation.settings

import androidx.lifecycle.ViewModel
import com.dusrel.shrinkit.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Settings state holder. All screens read/write the DataStore-backed
 * [UserPreferencesRepository], so separate instances per destination stay
 * consistent through the shared Flow.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    val prefs: UserPreferencesRepository
) : ViewModel()
