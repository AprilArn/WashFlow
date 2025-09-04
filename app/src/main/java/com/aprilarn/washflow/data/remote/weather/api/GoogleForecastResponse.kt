package com.aprilarn.washflow.data.remote.weather.api

// Data class utama untuk response forecast
data class GoogleForecastResponse(
    val forecastHours: List<ForecastHour>,
    val timeZone: TimeZONE // Bisa pakai ulang dari GoogleWeatherResponse.kt
)

// Data class untuk setiap jam dalam forecast
data class ForecastHour(
    val interval: Interval,
    val displayDateTime: DisplayDateTime,
    val isDaytime: Boolean,
    val weatherCondition: WeatherCondition, // Bisa pakai ulang dari GoogleWeatherResponse.kt
    val temperature: Temperature, // Bisa pakai ulang dari GoogleWeatherResponse.kt
    val feelsLikeTemperature: Temperature, // Direkomendasikan pakai ulang kelas Temperature
    val relativeHumidity: Int,
    val uvIndex: Int,
    val precipitation: ForecastPrecipitation, // Perlu dimodifikasi dari sebelumnya
    val thunderstormProbability: Int,
    val wind: Wind, // Bisa pakai ulang dari GoogleWeatherResponse.kt
    val cloudCover: Int
)

// Data class untuk interval waktu
data class Interval(
    val startTime: String,
    val endTime: String
)

// Data class untuk detail tanggal dan waktu
data class DisplayDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hours: Int,
    val utcOffset: String
)

// Data class untuk presipitasi dalam forecast (ada tambahan qpf)
data class ForecastPrecipitation(
    val probability: Probability, // Bisa pakai ulang dari GoogleWeatherResponse.kt
    val qpf: Qpf
)

// Data class baru untuk Quantitative Precipitation Forecast
data class Qpf(
    val quantity: Double,
    val unit: String
)