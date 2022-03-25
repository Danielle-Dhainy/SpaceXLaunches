package com.android.spacexlaunches.api

import com.android.spacexlaunches.models.Launch
import com.android.spacexlaunches.models.Rocket
import com.android.spacexlaunches.utils.Resource
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface APIService {

    @GET("launches")
    suspend fun loadLaunches(): Response<Launch>

    @GET("rockets")
    suspend fun getRocket(@Query("id") id: String?): Response<Rocket>

}