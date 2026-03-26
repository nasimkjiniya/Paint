package com.example.shapepaint.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.shapepaint.R
import com.example.shapepaint.data.remote.ReferenceApiService
import com.example.shapepaint.model.ReferenceArtwork
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ReferenceRepository {
    suspend fun searchArtworks(query: String): List<ReferenceArtwork>
    suspend fun downloadBitmap(url: String): Bitmap?
}

class DefaultReferenceRepository(
    private val apiService: ReferenceApiService,
    private val appContext: Context
) : ReferenceRepository {

    override suspend fun searchArtworks(query: String): List<ReferenceArtwork> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val maxResults = appContext.resources.getInteger(R.integer.reference_max_results)
            .takeIf { it > 0 } ?: FALLBACK_MAX_RESULTS
        runCatching {
            apiService.searchImages(
                query = trimmed,
                pageSize = maxResults
            )
        }.getOrNull()
            ?.results
            .orEmpty()
            .mapNotNull { image ->
                val thumbnailUrl = image.thumbnail?.takeIf { it.isNotBlank() }
                val importUrl = image.url?.takeIf { it.isNotBlank() } ?: thumbnailUrl
                val objectId = image.id?.takeIf { it.isNotBlank() }
                if (thumbnailUrl == null || importUrl == null || objectId == null) {
                    null
                } else {
                    ReferenceArtwork(
                        objectId = objectId,
                        title = image.title?.ifBlank { null }
                            ?: appContext.getString(R.string.reference_untitled_object),
                        artistName = image.creator?.ifBlank { null }
                            ?: appContext.getString(R.string.reference_unknown_artist),
                        objectDate = image.source?.ifBlank { null }
                            ?: image.license?.ifBlank { null }
                            ?: appContext.getString(R.string.reference_unknown_date),
                        thumbnailUrl = thumbnailUrl,
                        importUrl = importUrl
                    )
                }
            }
    }

    override suspend fun downloadBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            URL(url).openStream().use(BitmapFactory::decodeStream)
        }.getOrNull()
    }

    private companion object {
        const val FALLBACK_MAX_RESULTS = 18
    }
}
