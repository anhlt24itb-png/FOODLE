package com.foddy.app.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAT_KEY = doublePreferencesKey("last_lat")
        val LNG_KEY = doublePreferencesKey("last_lng")
        val ADDRESS_KEY = stringPreferencesKey("last_address")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    val userLocation: Flow<Triple<Double, Double, String>> = context.dataStore.data.map { preferences ->
        Triple(
            preferences[Keys.LAT_KEY] ?: 10.762622, // Default HCMC
            preferences[Keys.LNG_KEY] ?: 106.660172,
            preferences[Keys.ADDRESS_KEY] ?: "Hồ Chí Minh, Việt Nam"
        )
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_MODE_ENABLED] ?: false }

    suspend fun saveLocation(lat: Double, lng: Double, address: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAT_KEY] = lat
            preferences[Keys.LNG_KEY] = lng
            preferences[Keys.ADDRESS_KEY] = address
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DARK_MODE_ENABLED] = enabled
        }
    }
}
