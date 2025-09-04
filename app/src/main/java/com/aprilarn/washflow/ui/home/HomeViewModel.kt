////package com.aprilarn.washflow.ui.home
////
////import android.util.Log
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.aprilarn.washflow.data.remote.weather.service.WeatherApiService
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.flow.update
////import kotlinx.coroutines.launch
////import java.time.LocalDateTime
////import java.time.format.DateTimeFormatter
////import java.util.Calendar
////import java.util.Locale
////import kotlin.math.roundToInt
////
////class HomeViewModel : ViewModel() {
////    private val _uiState = MutableStateFlow(HomeUiState())
////    val uiState = _uiState.asStateFlow()
////
////    private val weatherApiService = WeatherApiService()
////
////    // Fungsi baru untuk mendapatkan sapaan dinamis
////    private fun getGreetingMessage(): String {
////        val calendar = Calendar.getInstance()
////        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
////            in 4..10 -> "Good Morning!"   // Dari jam 4 pagi sampai 10 pagi
////            in 11..14 -> "Good Afternoon!" // Dari jam 11 siang sampai 2 siang
////            in 15..17 -> "Good Evening!" // Dari jam 3 sore sampai 5 sore
////            else -> "Good Night!"      // Sisanya (jam 6 sore sampai 3 pagi)
////        }
////    }
////
////    fun fetchWeatherData(lat: Double, lon: Double) {
////        _uiState.update { it.copy(isLoading = true) }
////
////        viewModelScope.launch {
////            try {
////                // 1. Ambil data cuaca saat ini
////                val weatherResponse = weatherApiService.getWeatherData(lat = lat, lon = lon)
////                // 2. Ambil data prakiraan cuaca
////                val forecastResponse = weatherApiService.getForecastData(lat = lat, lon = lon)
////
////                // 3. Proses data prakiraan untuk ditampilkan di UI
////                val hourlyForecasts = forecastResponse.list.take(5).map { forecastItem ->
////                    // Format waktu dari "yyyy-MM-dd HH:mm:ss" menjadi "HH:mm" dengan manipulasi string
////                    val time = forecastItem.dt_txt.split(" ")[1].substring(0, 5) // -> "HH:mm"
////
////                    // Buat URL ikon
////                    val iconUrl = "https://openweathermap.org/img/wn/${forecastItem.weather.firstOrNull()?.icon}@2x.png"
////
////                    HourlyForecastUiState(
////                        time = time,
////                        iconUrl = iconUrl,
////                        temperature = "${forecastItem.main.temp.roundToInt()}°"
////                    )
////                }
////
////                _uiState.update { currentState ->
////                    currentState.copy(
////                        isLoading = false,
////                        greeting = getGreetingMessage(),
////                        weather = weatherResponse.weather.firstOrNull()?.description
////                            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } ?: "-",
////                        temperature = "${weatherResponse.main.temp.roundToInt()}°C",
////
////                        // url weather icon
////                        weatherIconUrl = "https://openweathermap.org/img/wn/${weatherResponse.weather.firstOrNull()?.icon}@4x.png", // Gunakan @4x untuk ukuran lebih besar
////
////                        recommendation = "Rekomendasi cuaca dimuat berdasarkan lokasimu saat ini.",
////                        inQueue = 12,
////                        onProcess = 3,
////                        done = 5,
////                        hourlyForecasts = hourlyForecasts
////                    )
////                }
////            } catch (e: Exception) {
////                Log.e("HomeViewModel", "Gagal mengambil data cuaca: ${e.message}")
////                _uiState.update {
////                    it.copy(
////                        isLoading = false,
////                        greeting = getGreetingMessage()
////                    )
////                }
////            }
////        }
////    }
////}
//
package com.aprilarn.washflow.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.remote.weather.service.WeatherApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val weatherApiService = WeatherApiService()

    // Fungsi sapaan dinamis (tidak ada perubahan)
    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Good Morning!"
            in 11..14 -> "Good Afternoon!"
            in 15..17 -> "Good Evening!"
            else -> "Good Night!"
        }
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // 1. Panggil endpoint API Google Weather yang baru
                val weatherResponse = weatherApiService.getCurrentConditions(lat = lat, lon = lon)
                val forecastResponse = weatherApiService.getHourlyForecastData(lat = lat, lon = lon)

                // 2. Proses data prakiraan per jam dari response baru
                val hourlyForecasts = forecastResponse.forecastHours.take(6).map { forecastItem ->
                    // Ambil jam dari displayDateTime dan format
                    val time = String.format(Locale.US, "%02d:00", forecastItem.displayDateTime.hours)

                    // Buat URL ikon dari iconBaseUri (tambahkan ekstensi .svg)
                    val iconUrl = "${forecastItem.weatherCondition.iconBaseUri}.png"

                    HourlyForecastUiState(
                        time = time,
                        iconUrl = iconUrl,
                        temperature = "${forecastItem.temperature.degrees.roundToInt()}°"
                    )
                }

                // 3. Update UI State dengan data dari Google Weather API
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        greeting = getGreetingMessage(),
                        // Ambil deskripsi cuaca dari struktur baru
                        weather = weatherResponse.weatherCondition.description.text,
                        // Ambil suhu dari struktur baru
                        temperature = "${weatherResponse.temperature.degrees.roundToInt()}°C",
                        // Buat URL ikon dari iconBaseUri (tambahkan ekstensi .svg)
                        weatherIconUrl = "${weatherResponse.weatherCondition.iconBaseUri}.png",
                        recommendation = "Rekomendasi cuaca dimuat berdasarkan lokasimu saat ini.",
                        inQueue = 12,
                        onProcess = 3,
                        done = 5,
                        hourlyForecasts = hourlyForecasts
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Gagal mengambil data cuaca: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        greeting = getGreetingMessage(),
                        weather = "Failed to load data",
                        recommendation = "Could not fetch weather data. Please check your connection."
                    )
                }
            }
        }
    }
}

//package com.aprilarn.washflow.ui.home
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.aprilarn.washflow.data.remote.weather.service.WeatherApiService
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.util.Calendar
//import java.util.Locale
//import kotlin.math.roundToInt
//
//class HomeViewModel : ViewModel() {
//    private val _uiState = MutableStateFlow(HomeUiState())
//    val uiState = _uiState.asStateFlow()
//    private val weatherApiService = WeatherApiService()
//
//    // Fungsi sapaan dinamis (tidak ada perubahan)
//    private fun getGreetingMessage(): String {
//        val calendar = Calendar.getInstance()
//        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
//            in 4..10 -> "Good Morning!"
//            in 11..14 -> "Good Afternoon!"
//            in 15..17 -> "Good Evening!"
//            else -> "Good Night!"
//        }
//    }
//
//    fun fetchWeatherData(lat: Double, lon: Double) {
//        _uiState.update { it.copy(isLoading = true) }
//
//        viewModelScope.launch {
//            try {
//                // 1. Panggil endpoint API Google Weather yang baru
//                val weatherResponse = weatherApiService.getCurrentConditions(lat = lat, lon = lon)
//                val forecastResponse = weatherApiService.getHourlyForecastData(lat = lat, lon = lon)
//
//                // 2. Proses data prakiraan per jam dari response baru
//                val hourlyForecasts = forecastResponse.forecastHours.take(7).map { forecastItem ->
//                    // Ambil jam dari displayDateTime dan format
//                    val time = String.format(Locale.US, "%02d:00", forecastItem.displayDateTime.hours)
//
//                    // Buat URL ikon dari iconBaseUri (tambahkan ekstensi .png)
//                    val iconUrl = "${forecastItem.weatherCondition.iconBaseUri}.png"
//
//                    // Ambil probabilitas presipitasi
//                    val precipitation = "${forecastItem.precipitation.probability.percent}%"
//
//                    HourlyForecastUiState(
//                        time = time,
//                        iconUrl = iconUrl,
//                        temperature = "${forecastItem.temperature.degrees.roundToInt()}°",
//                        precipitationProbability = precipitation
//                    )
//                }
//
//                // 3. Update UI State dengan data dari Google Weather API
//                _uiState.update { currentState ->
//                    currentState.copy(
//                        isLoading = false,
//                        greeting = getGreetingMessage(),
//                        // Ambil deskripsi cuaca dari struktur baru
//                        weather = weatherResponse.weatherCondition.description.text,
//                        // Ambil suhu dari struktur baru
//                        temperature = "${weatherResponse.temperature.degrees.roundToInt()}°C",
//                        // Buat URL ikon dari iconBaseUri (tambahkan ekstensi .png)
//                        weatherIconUrl = "${weatherResponse.weatherCondition.iconBaseUri}.png",
//                        recommendation = "Rekomendasi cuaca dimuat berdasarkan lokasimu saat ini.",
//                        inQueue = 12,
//                        onProcess = 3,
//                        done = 5,
//                        hourlyForecasts = hourlyForecasts
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e("HomeViewModel", "Gagal mengambil data cuaca: ${e.message}", e)
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        greeting = getGreetingMessage(),
//                        weather = "Failed to load data",
//                        recommendation = "Could not fetch weather data. Please check your connection."
//                    )
//                }
//            }
//        }
//    }
//}