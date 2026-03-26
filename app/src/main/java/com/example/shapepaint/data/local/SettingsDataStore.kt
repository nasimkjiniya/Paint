package com.example.shapepaint.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "shape_paint_settings")
