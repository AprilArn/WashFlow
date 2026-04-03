package com.aprilarn.washflow.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.BuildConfig
import com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService
import com.aprilarn.washflow.data.remote.weather.service.WeatherApiService
import com.aprilarn.washflow.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class HomeViewModel(
    private val orderRepository: OrderRepository,
    private val geocodingApiService: GeocodingApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val weatherApiService = WeatherApiService()

    init {
        listenForOrderUpdates()
    }

    private fun listenForOrderUpdates() {
        viewModelScope.launch {
            orderRepository.getOrdersRealtime()
                .catch { e ->
                    Log.e("HomeViewModel", "Error listening for order updates", e)
                }
                .collect { orders ->
                    // Hitung jumlah order berdasarkan status
                    val groupedOrders = orders.groupBy { it.status }
                    _uiState.update {
                        it.copy(
                            inQueue = groupedOrders["On Queue"]?.size ?: 0,
                            onProcess = groupedOrders["On Process"]?.size ?: 0,
                            done = groupedOrders["Done"]?.size ?: 0
                        )
                    }
                }
        }
    }

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

    fun fetchWeatherData(lat: Double, lon: Double, isGps: Boolean = true) {
        _uiState.update { it.copy(isLoading = true, isGpsLocation = isGps) }

        viewModelScope.launch {
            try {
                // Panggil endpoint API Google Weather yang baru
                val weatherResponse = weatherApiService.getCurrentConditions(lat = lat, lon = lon)
                val forecastResponse = weatherApiService.getHourlyForecastData(lat = lat, lon = lon)

                // Panggil Geocoding API untuk dapatkan alamat asli
                val geoResponse = geocodingApiService.getAddressFromLocation("$lat,$lon", BuildConfig.API_KEY)
                var addressText = "Lokasi tidak diketahui"
                if (geoResponse.status == "OK" && geoResponse.results.isNotEmpty()) {
                    val fullAddress = geoResponse.results[0].formattedAddress
                    val parts = fullAddress.split(",").map { it.trim() }
                    addressText = if (parts.size >= 2) {
                        parts.dropLast(1).takeLast(3).joinToString(", ") // Format 3 kata
                    } else {
                        fullAddress
                    }
                }

                // Proses data prakiraan per jam dari response baru
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

                // Update UI State dengan data dari Google Weather API
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        greeting = getGreetingMessage(),
                        weather = weatherResponse.weatherCondition.description.text,
                        temperature = "${weatherResponse.temperature.degrees.roundToInt()}°C",
                        weatherIconUrl = "${weatherResponse.weatherCondition.iconBaseUri}.png",
                        locationName = addressText,
                        recommendation = "Rekomendasi cuaca dimuat berdasarkan lokasimu saat ini.",
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