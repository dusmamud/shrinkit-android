package com.dusrel.shrinkit.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultQuality: Int = 90,
    val defaultFormatName: String = "JPEG",
    val notificationsEnabled: Boolean = true
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "shrinkit_prefs"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_QUALITY = intPreferencesKey("default_quality")
        val DEFAULT_FORMAT = stringPreferencesKey("default_format")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e ->
            // Corrupted prefs file must not crash collectors; fall back to defaults.
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserPreferences(
                onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
                themeMode = runCatching {
                    ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
                }.getOrDefault(ThemeMode.SYSTEM),
                defaultQuality = (prefs[Keys.DEFAULT_QUALITY] ?: 90).coerceIn(1, 100),
                defaultFormatName = prefs[Keys.DEFAULT_FORMAT] ?: "JPEG",
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
            )
        }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDefaultQuality(quality: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_QUALITY] = quality.coerceIn(1, 100) }
    }

    suspend fun setDefaultFormatName(name: String) {
        context.dataStore.edit { it[Keys.DEFAULT_FORMAT] = name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
