// com/aprilarn/washflow/ui/home/HomeViewModel.kt
package com.aprilarn.washflow.ui.home

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.BuildConfig
import com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService
import com.aprilarn.washflow.data.remote.weather.service.WeatherApiService
import com.aprilarn.washflow.data.repository.OrderRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeViewModel(
    private val orderRepository: OrderRepository,
    private val geocodingApiService: GeocodingApiService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val weatherApiService = WeatherApiService()
    private val gson = Gson()

    init {
        listenForOrderUpdates()
        startPeriodicRefresh()
    }

    private fun listenForOrderUpdates() {
        viewModelScope.launch {
            orderRepository.getOrdersRealtime()
                .catch { e ->
                    Log.e("HomeViewModel", "Error listening for order updates", e)
                }
                .collect { orders ->
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

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val minutes = now.get(Calendar.MINUTE)
                val seconds = now.get(Calendar.SECOND)
                val millis = now.get(Calendar.MILLISECOND)

                val nextTargetMinute = if (minutes < 30) 30 else 60
                val delayMinutes = nextTargetMinute - minutes - 1
                val delaySeconds = 60 - seconds
                val delayMillis = (delayMinutes * 60 + delaySeconds) * 1000L - millis

                delay(delayMillis)

                Log.d("HomeViewModel", "Triggering scheduled auto-refresh at ${Calendar.getInstance().time}")
                val lastLat = sharedPreferences.getFloat("LAST_LAT", 0f).toDouble()
                val lastLon = sharedPreferences.getFloat("LAST_LON", 0f).toDouble()
                val isGps = _uiState.value.isGpsLocation

                if (lastLat != 0.0 || lastLon != 0.0) {
                    fetchWeatherData(lastLat, lastLon, isGps)
                } else {
                    _uiState.update { it.copy(greeting = getGreetingMessage()) }
                }
            }
        }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Good Morning!"
            in 11..15 -> "Good Afternoon!"
            in 16..20 -> "Good Evening!"
            else -> "Good Night!"
        }
    }

    private fun getTimestampSlot(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val year = cal.get(Calendar.YEAR)
        val day = cal.get(Calendar.DAY_OF_YEAR)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minuteSlot = if (cal.get(Calendar.MINUTE) < 30) "00" else "30"
        return "$year-$day-$hour-$minuteSlot"
    }

    fun fetchWeatherData(lat: Double, lon: Double, isGps: Boolean = true) {
        val currentTime = System.currentTimeMillis()
        val lastFetchTime = sharedPreferences.getLong("LAST_FETCH_TIME", 0L)
        val lastLat = sharedPreferences.getFloat("LAST_LAT", 0f).toDouble()
        val lastLon = sharedPreferences.getFloat("LAST_LON", 0f).toDouble()

        val isSameLocation = abs(lastLat - lat) < 0.01 && abs(lastLon - lon) < 0.01

        val currentSlot = getTimestampSlot(currentTime)
        val lastSlot = getTimestampSlot(lastFetchTime)
        val isCacheValid = currentSlot == lastSlot

        if (isSameLocation && isCacheValid) {
            Log.d("HomeViewModel", "Menggunakan data cuaca dari Cache Memori")

            val savedWeather = sharedPreferences.getString("WEATHER_DESC", "Unknown") ?: "Unknown"
            val savedTemp = sharedPreferences.getString("TEMP", "--°C") ?: "--°C"
            val savedIcon = sharedPreferences.getString("ICON_URL", "") ?: ""
            val savedLocName = sharedPreferences.getString("LOCATION_NAME", "Unknown Location") ?: "Unknown Location"
            val savedRec = sharedPreferences.getString("RECOMMENDATION", "Rekomendasi dimuat...") ?: "Rekomendasi dimuat..."

            val savedHourlyJson = sharedPreferences.getString("HOURLY_JSON", "[]")
            val type = object : TypeToken<List<HourlyForecastUiState>>() {}.type
            val savedHourly: List<HourlyForecastUiState> = try {
                gson.fromJson(savedHourlyJson, type)
            } catch (e: Exception) { emptyList() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    greeting = getGreetingMessage(),
                    weather = savedWeather,
                    temperature = savedTemp,
                    weatherIconUrl = savedIcon,
                    locationName = savedLocName,
                    recommendation = savedRec,
                    isGpsLocation = isGps,
                    hourlyForecasts = savedHourly
                )
            }
            return
        }

        Log.d("HomeViewModel", "Mengambil data cuaca baru dari API...")
        _uiState.update { it.copy(isLoading = true, isGpsLocation = isGps) }

        viewModelScope.launch {
            try {
                val weatherResponse = weatherApiService.getCurrentConditions(lat = lat, lon = lon)
                val forecastResponse = weatherApiService.getHourlyForecastData(lat = lat, lon = lon)

                val geoResponse = geocodingApiService.getAddressFromLocation("$lat,$lon", BuildConfig.API_KEY)
                var addressText = "Lokasi tidak diketahui"
                if (geoResponse.status == "OK" && geoResponse.results.isNotEmpty()) {
                    val fullAddress = geoResponse.results[0].formattedAddress
                    val parts = fullAddress.split(",").map { it.trim() }
                    addressText = if (parts.size >= 2) {
                        parts.dropLast(1).takeLast(3).joinToString(", ")
                    } else {
                        fullAddress
                    }
                }

                val hourlyForecasts = forecastResponse.forecastHours.take(6).map { forecastItem ->
                    val time = String.format(Locale.US, "%02d:00", forecastItem.displayDateTime.hours)
                    val iconUrl = "${forecastItem.weatherCondition.iconBaseUri}.png"

                    HourlyForecastUiState(
                        time = time,
                        iconUrl = iconUrl,
                        temperature = "${forecastItem.temperature.degrees.roundToInt()}°"
                    )
                }

                // Logika rekomendasi (bisa kamu ganti dengan engine AI nantinya)
                val newRecommendation = "Pastikan semua cucian disimpan dalam ruang tertutup atau diberi pelindung."

                sharedPreferences.edit().apply {
                    putLong("LAST_FETCH_TIME", currentTime)
                    putFloat("LAST_LAT", lat.toFloat())
                    putFloat("LAST_LON", lon.toFloat())
                    putString("WEATHER_DESC", weatherResponse.weatherCondition.description.text)
                    putString("TEMP", "${weatherResponse.temperature.degrees.roundToInt()}°C")
                    putString("ICON_URL", "${weatherResponse.weatherCondition.iconBaseUri}.png")
                    putString("LOCATION_NAME", addressText)
                    putString("RECOMMENDATION", newRecommendation)
                    putString("HOURLY_JSON", gson.toJson(hourlyForecasts))
                    apply()
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        greeting = getGreetingMessage(),
                        weather = weatherResponse.weatherCondition.description.text,
                        temperature = "${weatherResponse.temperature.degrees.roundToInt()}°C",
                        weatherIconUrl = "${weatherResponse.weatherCondition.iconBaseUri}.png",
                        locationName = addressText,
                        isGpsLocation = isGps,
                        recommendation = newRecommendation,
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
                        locationName = "Gagal memuat alamat",
                        recommendation = "Gagal memuat rekomendasi"
                    )
                }
            }
        }
    }
}