package com.android.spacexlaunches.repository

import com.android.spacexlaunches.api.RetrofitInstance

class Repository {

    suspend fun loadLaunches() = RetrofitInstance.api.loadLaunches()
    suspend fun getRocket(id:String) = RetrofitInstance.api.getRocket(id)
}