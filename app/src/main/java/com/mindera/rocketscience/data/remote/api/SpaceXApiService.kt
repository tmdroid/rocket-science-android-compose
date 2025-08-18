package com.mindera.rocketscience.data.remote.api

import com.mindera.rocketscience.data.remote.dto.LaunchDto
import retrofit2.http.GET

interface SpaceXApiService {
    
    @GET("launches")
    suspend fun getAllLaunches(): List<LaunchDto>
    
    companion object {
        const val BASE_URL = "https://api.spacexdata.com/v3/"
    }
}