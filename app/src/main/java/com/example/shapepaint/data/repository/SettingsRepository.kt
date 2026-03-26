package com.example.shapepaint.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.shapepaint.R
import com.example.shapepaint.data.local.settingsDataStore
import com.example.shapepaint.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val defaultArtistName: String
    val defaultSize: Int
    val settings: Flow<UserSettings>
    suspend fun save(settings: UserSettings)
}

class DefaultSettingsRepository(
    private val context: Context
) : SettingsRepository {
    override val defaultArtistName: String
        get() = context.getString(R.string.default_artist_name)

    override val defaultSize: Int
        get() = context.resources.getInteger(R.integer.default_shape_size)

    override val settings: Flow<UserSettings> = context.settingsDataStore.data.map { preferences ->
        UserSettings(
            artistName = preferences[ARTIST_NAME] ?: defaultArtistName,
            showGrid = preferences[SHOW_GRID] ?: true,
            showShapeLabels = preferences[SHOW_SHAPE_LABELS] ?: true,
            defaultSize = preferences[DEFAULT_SIZE] ?: defaultSize
        )
    }

    override suspend fun save(settings: UserSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[ARTIST_NAME] = settings.artistName.ifBlank { defaultArtistName }
            preferences[SHOW_GRID] = settings.showGrid
            preferences[SHOW_SHAPE_LABELS] = settings.showShapeLabels
            preferences[DEFAULT_SIZE] = settings.defaultSize.takeIf { it > 0 } ?: defaultSize
        }
    }

    private companion object {
        val ARTIST_NAME = stringPreferencesKey("artist_name")
        val SHOW_GRID = booleanPreferencesKey("show_grid")
        val SHOW_SHAPE_LABELS = booleanPreferencesKey("show_shape_labels")
        val DEFAULT_SIZE = intPreferencesKey("default_size")
    }
}
