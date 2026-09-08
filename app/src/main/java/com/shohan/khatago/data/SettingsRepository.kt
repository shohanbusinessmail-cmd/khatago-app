package com.shohan.khatago.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shohan.khatago.domain.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "khatago_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val name = stringPreferencesKey("profile_name")
        val currency = stringPreferencesKey("currency_code")
        val onboarding = booleanPreferencesKey("onboarding_complete")
        val notifications = booleanPreferencesKey("notifications_enabled")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            name = prefs[Keys.name].orEmpty(),
            currencyCode = prefs[Keys.currency] ?: "BDT",
            onboardingComplete = prefs[Keys.onboarding] ?: false,
            notificationsEnabled = prefs[Keys.notifications] ?: true
        )
    }

    suspend fun updateName(name: String) = context.settingsDataStore.edit { it[Keys.name] = name.trim() }
    suspend fun updateCurrency(code: String) = context.settingsDataStore.edit { it[Keys.currency] = code }
    suspend fun setOnboardingComplete(value: Boolean) = context.settingsDataStore.edit { it[Keys.onboarding] = value }
    suspend fun setNotificationsEnabled(value: Boolean) = context.settingsDataStore.edit { it[Keys.notifications] = value }
    suspend fun clear() = context.settingsDataStore.edit { it.clear() }
}
