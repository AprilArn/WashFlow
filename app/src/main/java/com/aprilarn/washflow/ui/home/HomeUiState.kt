package com.aprilarn.washflow.ui.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "--!",
    val weather: String = "loading weather...",
    val temperature: String = "--°C",
    val recommendation: String = "loading recommendation...",
    val inQueue: Int = 0,
    val onProcess: Int = 0,
    val done: Int = 0,
    val weatherIconUrl: String = "",
    val hourlyForecasts: List<HourlyForecastUiState> = emptyList()
)