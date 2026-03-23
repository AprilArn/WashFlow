package com.aprilarn.washflow.data.remote.weather.api

import com.google.gson.annotations.SerializedName

data class GoogleGeocodingResponse(
    @SerializedName("results") val results: List<GeocodingResult>,
    @SerializedName("status") val status: String
)

data class GeocodingResult(
    @SerializedName("formatted_address") val formattedAddress: String
)