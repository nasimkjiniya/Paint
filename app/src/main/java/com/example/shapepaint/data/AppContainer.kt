package com.example.shapepaint.data

import android.content.Context
import com.example.shapepaint.data.local.ShapePaintDatabase
import com.example.shapepaint.data.repository.DefaultProjectRepository
import com.example.shapepaint.data.repository.DefaultReferenceRepository
import com.example.shapepaint.data.repository.DefaultSettingsRepository
import com.example.shapepaint.data.repository.ProjectRepository
import com.example.shapepaint.data.repository.ReferenceRepository
import com.example.shapepaint.data.repository.SettingsRepository
import com.example.shapepaint.data.remote.ReferenceApiService
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface AppContainer {
    val projectRepository: ProjectRepository
    val referenceRepository: ReferenceRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database = ShapePaintDatabase.getInstance(context)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openverse.org/v1/")
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
        .build()

    private val referenceApiService = retrofit.create(ReferenceApiService::class.java)

    override val settingsRepository: SettingsRepository by lazy {
        DefaultSettingsRepository(context)
    }

    override val projectRepository: ProjectRepository by lazy {
        DefaultProjectRepository(
            projectDao = database.projectDao(),
            settingsRepository = settingsRepository,
            appContext = context.applicationContext
        )
    }

    override val referenceRepository: ReferenceRepository by lazy {
        DefaultReferenceRepository(
            apiService = referenceApiService,
            appContext = context.applicationContext
        )
    }
}
