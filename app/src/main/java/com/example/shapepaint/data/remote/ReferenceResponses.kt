package com.example.shapepaint.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReferenceSearchResponse(
    @Json(name = "result_count") val resultCount: Int = 0,
    @Json(name = "results") val results: List<ReferenceImageResponse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ReferenceImageResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "creator") val creator: String? = null,
    @Json(name = "license") val license: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "url") val url: String? = null
)
