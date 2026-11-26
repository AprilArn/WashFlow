package com.aprilarn.washflow.ui.feature.home

import androidx.annotation.StringRes
import com.aprilarn.washflow.R

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "--!",
    @param:StringRes val greetingResId: Int? = null,
    val weather: String = "",
    @param:StringRes val weatherFallbackResId: Int? = R.string.home_loading_weather,
    val weatherType: String = "",
    val isDaytime: Boolean? = null,
    val temperature: String = "--°C",
    val locationName: String = "",
    @param:StringRes val locationNameResId: Int? = R.string.settings_finding_location,
    val isGpsLocation: Boolean = true,
    val recommendation: String = "",
    @param:StringRes val recommendationResId: Int? = R.string.home_loading_recommendation,
    val inQueue: Int = 0,
    val onProcess: Int = 0,
    val done: Int = 0,
    val weatherIconUrl: String = "",
    val hourlyForecasts: List<HourlyForecastUiState> = emptyList(),

    // Weather Details
    val humidity: String = "--%",
    val uvIndex: String = "--",
    val precipitationProb: String = "--%",
    val windSpeed: String = "-- km/h",
    val feelsLike: String = "--°C",
    val thunderstormProb: String = "--%",
    val windDirection: String = "--"
)
