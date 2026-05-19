package com.aprilarn.washflow.data.remote.weather.api


data class GoogleDailyForecastResponse(
    val forecastDays: List<ForecastDays>,
    val timeZone: TimeZONE // Reuse dari GoogleCurrentWeatherResponse.kt
)

data class ForecastDays(
    val interval: Interval, // Reuse dari GoogleHourlyForecastResponse.kt
    val displayDate: DisplayDate,
    val daytimeForecast: DaytimeForecast,
    val nighttimeForecast: NighttimeForecast,
    val maxTemperature: TemperatureHistoryValue, // Reuse dari GoogleCurrentWeatherResponse.kt
    val minTemperature: TemperatureHistoryValue,

    // Gunakan ulang Temperature agar tidak membuat banyak class duplikat
    val feelsLikeMaxTemperature: Temperature,
    val feelsLikeMinTemperature: Temperature,

    val sunEvents: SunEvents,
    val moonEvents: MoonEvents,

    val maxHeatIndex: Temperature, // Reuse Temperature
    val iceThickness: IceThickness,
)

data class DisplayDate(
    val year: Int,
    val month: Int,
    val day: Int
)

data class DaytimeForecast(
    val interval: Interval,
    val weatherCondition: WeatherCondition,
    val relativeHumidity: Int,
    val uvIndex: Int,
    // Gunakan ForecastPrecipitation (bukan Precipitation biasa) karena ada QPF
    val precipitation: ForecastPrecipitation,
    val thunderstormProbability: Int,
    val wind: Wind,
    val cloudCover: Int
)

data class NighttimeForecast(
    val interval: Interval,
    val weatherCondition: WeatherCondition,
    val relativeHumidity: Int,
    val uvIndex: Int,
    // Gunakan ForecastPrecipitation karena ada QPF
    val precipitation: ForecastPrecipitation,
    val thunderstormProbability: Int,
    val wind: Wind,
    val cloudCover: Int
)

data class SunEvents(
    val sunriseTime: String,
    val sunsetTime: String
)

data class MoonEvents(
    val moonPhase: String,
    val moonriseTimes: List<String>,
    val moonsetTimes: List<String>
)

data class IceThickness(
    val thickness: Double, // Gunakan Double untuk berjaga-jaga jika ada nilai desimal
    val unit: String
)