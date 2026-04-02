package com.aprilarn.washflow.data.remote.weather.service

import com.aprilarn.washflow.data.remote.weather.api.GoogleGeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("maps/api/geocode/json")
    suspend fun getAddressFromLocation(
        @Query("latlng") latlng: String, // Format: "latitude,longitude"
        @Query("key") apiKey: String
    ): GoogleGeocodingResponse
}