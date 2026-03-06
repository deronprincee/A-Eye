package com.example.aeye.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aeye.data.model.CalibrationDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalibrationViewModel(
    private val calibrationDataStore: CalibrationDataStore
) : ViewModel() {

    val pxPerMm: StateFlow<Double?> =
        calibrationDataStore.pxPerMmFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun setCalibration(pxPerMm: Double) {
        viewModelScope.launch { calibrationDataStore.savePxPerMm(pxPerMm) }
    }

    fun clearCalibration() {
        viewModelScope.launch { calibrationDataStore.clearPxPerMm() }
    }
}