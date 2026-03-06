package com.example.aeye.data.model

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "aeye_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object CalibrationKeys {
    val PX_PER_MM = doublePreferencesKey("px_per_mm")
}

class CalibrationDataStore(private val context: Context) {

    val pxPerMmFlow: Flow<Double?> =
        context.dataStore.data.map { prefs -> prefs[CalibrationKeys.PX_PER_MM] }

    suspend fun savePxPerMm(pxPerMm: Double) {
        context.dataStore.edit { prefs ->
            prefs[CalibrationKeys.PX_PER_MM] = pxPerMm
        }
    }

    suspend fun clearPxPerMm() {
        context.dataStore.edit { prefs ->
            prefs.remove(CalibrationKeys.PX_PER_MM)
        }
    }
}