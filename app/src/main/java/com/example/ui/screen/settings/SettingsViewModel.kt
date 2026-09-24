package com.example.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesRepository
import com.example.model.GaugeDisplayMode
import com.example.model.LengthUnit
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository(application)

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setLengthUnit(unit: LengthUnit) {
        viewModelScope.launch {
            repository.updateLengthUnit(unit)
        }
    }

    fun setGaugeDisplayMode(mode: GaugeDisplayMode) {
        viewModelScope.launch {
            repository.updateGaugeDisplayMode(mode)
        }
    }
}
