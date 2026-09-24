package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.GaugeDisplayMode
import com.example.model.LengthUnit
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "coil_settings")

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LENGTH_UNIT = stringPreferencesKey("length_unit")
        val GAUGE_DISPLAY_MODE = stringPreferencesKey("gauge_display_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val lengthUnitStr = preferences[PreferencesKeys.LENGTH_UNIT] ?: LengthUnit.MM.name
            val lengthUnit = try {
                LengthUnit.valueOf(lengthUnitStr)
            } catch (e: Exception) {
                LengthUnit.MM
            }

            val gaugeModeStr = preferences[PreferencesKeys.GAUGE_DISPLAY_MODE] ?: GaugeDisplayMode.BOTH.name
            val gaugeMode = try {
                GaugeDisplayMode.valueOf(gaugeModeStr)
            } catch (e: Exception) {
                GaugeDisplayMode.BOTH
            }

            UserPreferences(
                lengthUnit = lengthUnit,
                gaugeDisplayMode = gaugeMode
            )
        }

    suspend fun updateLengthUnit(unit: LengthUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LENGTH_UNIT] = unit.name
        }
    }

    suspend fun updateGaugeDisplayMode(mode: GaugeDisplayMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAUGE_DISPLAY_MODE] = mode.name
        }
    }
}
