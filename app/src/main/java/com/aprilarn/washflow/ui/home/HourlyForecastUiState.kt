package com.aprilarn.washflow.ui.home

data class HourlyForecastUiState(
    val time: String,
    val iconUrl: String,
    val temperature: String,
    val isEvent: Boolean = false,
    val eventLabel: String? = null
)