package com.example.shapepaint.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ReferenceApiService {
    @GET("images/")
    suspend fun searchImages(
        @Query("q") query: String,
        @Query("page_size") pageSize: Int
    ): ReferenceSearchResponse
}
