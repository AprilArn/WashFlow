package com.aprilarn.washflow.data.remote.weather.api


data class GoogleWeatherResponse(
    val currentTime: String,
    val timeZone: TimeZONE,
    val weatherCondition: WeatherCondition,
    val temperature: Temperature,
    val feelsLikeTemperature: FeelsLikeTemperature,
    val windChill: WindChill,
    val relativeHumidity: Int,
    val uvIndex: Int,
    val precipitation: Precipitation,
    val thunderstormProbability: Int,
    val wind: Wind,
    val currentConditionsHistory: CurrentConditionsHistory

)

data class TimeZONE(
    val id: String
)

data class WeatherCondition(
    val iconBaseUri: String,
    val description: Description,
    val type: String
) data class Description (
    val text: String,
    val languageCode: String
)

data class Temperature (
    val degrees: Float,
    val unit: String
)

data class FeelsLikeTemperature (
    val degrees: Float,
    val unit: String
)

data class WindChill (
    val degrees: Float,
    val unit: String
)

data class Precipitation(
    val probability: Probability
) data class Probability (
    val percent: Int,
    val type: String
)

data class Wind(
    val direction: WindDirection,
    val speed: WindValue,
    val gust: WindValue
) data class WindDirection(
    val degrees: Int,
    val cardinal: String
) data class WindValue(
    val value: Double,
    val unit: String
)

data class CurrentConditionsHistory(
    val temperatureChange: TemperatureHistoryValue,
    val maxTemperature: TemperatureHistoryValue,
    val minTemperature: TemperatureHistoryValue
) data class TemperatureHistoryValue(
    val degrees: Double,
    val unit: String
)



