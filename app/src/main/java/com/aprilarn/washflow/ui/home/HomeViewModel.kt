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

    private var currentWeatherOnlyForecast: List<HourlyForecastUiState> = emptyList()
    private var todayDeadlines: List<HourlyForecastUiState> = emptyList()

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
                    
                    // Filter for deadlines that fall within our forecast window
                    val now = Calendar.getInstance()
                    val windowEnd = Calendar.getInstance().apply { 
                        add(Calendar.HOUR_OF_DAY, 12) // Buffer matching weather fetch
                    }

                    todayDeadlines = orders.filter { order ->
                        order.orderDueDate != null && order.status != "Done" && order.status != "Canceled"
                    }.filter { order ->
                        val dueCal = Calendar.getInstance().apply { time = order.orderDueDate!!.toDate() }
                        dueCal.after(now) && dueCal.before(windowEnd)
                    }.map { order ->
                        val dueCal = Calendar.getInstance().apply { time = order.orderDueDate!!.toDate() }
                        val timeStr = String.format(Locale.US, "%02d:%02d", dueCal.get(Calendar.HOUR_OF_DAY), dueCal.get(Calendar.MINUTE))
                        HourlyForecastUiState(
                            time = timeStr,
                            iconUrl = "WS_DEADLINE",
                            temperature = "--",
                            isEvent = true,
                            eventLabel = "Deadline"
                        )
                    }

                    _uiState.update {
                        it.copy(
                            inQueue = groupedOrders["On Queue"]?.size ?: 0,
                            onProcess = groupedOrders["On Process"]?.size ?: 0,
                            done = groupedOrders["Done"]?.size ?: 0,
                            hourlyForecasts = injectEvents(currentWeatherOnlyForecast)
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

    private fun injectEvents(
        weatherForecasts: List<HourlyForecastUiState>
    ): List<HourlyForecastUiState> {
        val result = weatherForecasts.toMutableList()

        fun timeToMinutes(timeStr: String): Int {
            val parts = timeStr.split(":").map { it.toIntOrNull() ?: 0 }
            return if (parts.size >= 2) parts[0] * 60 + parts[1] else 0
        }

        val openTime = sharedPreferences.getString("WS_OPEN_TIME", null)
        val closeTime = sharedPreferences.getString("WS_CLOSE_TIME", null)
        val sunriseTime = sharedPreferences.getString("SUNRISE_TIME", null)
        val sunsetTime = sharedPreferences.getString("SUNSET_TIME", null)

        if (weatherForecasts.isNotEmpty()) {
            val startMin = timeToMinutes(weatherForecasts.first().time)
            val endMin = timeToMinutes(weatherForecasts.last().time)

            fun isInWindow(time: String): Boolean {
                val min = timeToMinutes(time)
                return min >= startMin && min <= endMin
            }

            fun getEventTimestamp(timeStr: String): Long {
                val parts = timeStr.split(":").map { it.toIntOrNull() ?: 0 }
                val hour = if (parts.isNotEmpty()) parts[0] else 0
                val minute = if (parts.size >= 2) parts[1] else 0
                
                return Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }

            if (openTime != null && isInWindow(openTime)) {
                result.add(
                    HourlyForecastUiState(
                        time = openTime,
                        iconUrl = "WS_OPEN",
                        temperature = "--",
                        isEvent = true,
                        eventLabel = "Open",
                        timestamp = getEventTimestamp(openTime)
                    )
                )
            }

            if (closeTime != null && isInWindow(closeTime)) {
                result.add(
                    HourlyForecastUiState(
                        time = closeTime,
                        iconUrl = "WS_CLOSE",
                        temperature = "--",
                        isEvent = true,
                        eventLabel = "Closed",
                        timestamp = getEventTimestamp(closeTime)
                    )
                )
            }
            
            if (sunriseTime != null && isInWindow(sunriseTime)) {
                result.add(
                    HourlyForecastUiState(
                        time = sunriseTime,
                        iconUrl = "WS_SUNRISE",
                        temperature = "--",
                        isEvent = true,
                        eventLabel = "Sunrise",
                        timestamp = getEventTimestamp(sunriseTime)
                    )
                )
            }

            if (sunsetTime != null && isInWindow(sunsetTime)) {
                result.add(
                    HourlyForecastUiState(
                        time = sunsetTime,
                        iconUrl = "WS_SUNSET",
                        temperature = "--",
                        isEvent = true,
                        eventLabel = "Sunset",
                        timestamp = getEventTimestamp(sunsetTime)
                    )
                )
            }
            
            // Add deadlines that are within the forecast window
            todayDeadlines.forEach { deadline ->
                if (isInWindow(deadline.time)) {
                    result.add(deadline.copy(timestamp = getEventTimestamp(deadline.time)))
                }
            }
        }

        // Sort: Chronological, then Weather (0) -> Sunrise/Sunset (1) -> Open/Close (2) -> Deadline (3)
        val sortedAll = result.sortedWith(
            compareBy<HourlyForecastUiState> { it.timestamp }
                .thenBy { item ->
                    when {
                        !item.isEvent -> 0
                        item.iconUrl == "WS_SUNRISE" || item.iconUrl == "WS_SUNSET" -> 1
                        item.iconUrl == "WS_OPEN" || item.iconUrl == "WS_CLOSE" -> 2
                        item.iconUrl == "WS_DEADLINE" -> 3
                        else -> 4
                    }
                }
        )

        // Always return exactly 8 items. 
        // If events were added and total > 8, the items furthest in time are dropped.
        return sortedAll.take(8) // ubah pada bagian WeatherApiService juga jika nilai ini diubah
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

            val savedHumidity = sharedPreferences.getString("HUMIDITY", "--%") ?: "--%"
            val savedUV = sharedPreferences.getString("UV_INDEX", "--") ?: "--"
            val savedPrecip = sharedPreferences.getString("PRECIP_PROB", "--%") ?: "--%"
            val savedWind = sharedPreferences.getString("WIND_SPEED", "-- km/h") ?: "-- km/h"
            val savedFeels = sharedPreferences.getString("FEELS_LIKE", "--°C") ?: "--°C"
            val savedThunder = sharedPreferences.getString("THUNDER_PROB", "--%") ?: "--%"
            val savedWindDir = sharedPreferences.getString("WIND_DIRECTION", "--") ?: "--"

            val savedHourlyJson = sharedPreferences.getString("HOURLY_JSON", "[]")
            val type = object : TypeToken<List<HourlyForecastUiState>>() {}.type
            val savedHourly: List<HourlyForecastUiState> = try {
                gson.fromJson(savedHourlyJson, type)
            } catch (e: Exception) { emptyList() }

            val currentTimeMillis = System.currentTimeMillis()
            
            // Filter out items that are past
            val filteredHourly = savedHourly.filter { item ->
                item.timestamp > currentTimeMillis
            }.take(12)

            // Inject operational hours into cached data as well
            currentWeatherOnlyForecast = filteredHourly
            val combinedForecast = injectEvents(filteredHourly)

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
                    hourlyForecasts = combinedForecast,
                    humidity = savedHumidity,
                    uvIndex = savedUV,
                    precipitationProb = savedPrecip,
                    windSpeed = savedWind,
                    feelsLike = savedFeels,
                    thunderstormProb = savedThunder,
                    windDirection = savedWindDir
                )
            }
            return
        }

        Log.d("HomeViewModel", "Mengambil data cuaca baru dari API...")
        _uiState.update { it.copy(isLoading = true, isGpsLocation = isGps) }

        viewModelScope.launch {
            try {
                val weatherResponse = weatherApiService.getCurrentConditions(lat = lat, lon = lon)
                val forecastResponse = weatherApiService.getHourlyForecastData(lat = lat, lon = lon, hours = 12)
                val dailyResponse = weatherApiService.getDailyForecastData(lat = lat, lon = lon, days = 1)

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

                val now = Calendar.getInstance()
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val currentDay = now.get(Calendar.DAY_OF_MONTH)

                val hourlyForecasts = forecastResponse.forecastHours
                    .filter { forecastItem ->
                        if (forecastItem.displayDateTime.day == currentDay) {
                            forecastItem.displayDateTime.hours > currentHour
                        } else {
                            true // Next day(s)
                        }
                    }
                    .take(12) // Get more items initially to account for event injections
                    .map { forecastItem ->
                        val time = String.format(Locale.US, "%02d:00", forecastItem.displayDateTime.hours)
                        val iconUrl = "${forecastItem.weatherCondition.iconBaseUri}.png"
                        
                        // Create a timestamp for reliable sorting
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, forecastItem.displayDateTime.year)
                            set(Calendar.MONTH, forecastItem.displayDateTime.month - 1)
                            set(Calendar.DAY_OF_MONTH, forecastItem.displayDateTime.day)
                            set(Calendar.HOUR_OF_DAY, forecastItem.displayDateTime.hours)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        HourlyForecastUiState(
                            time = time,
                            iconUrl = iconUrl,
                            temperature = "${forecastItem.temperature.degrees.roundToInt()}°",
                            timestamp = calendar.timeInMillis
                        )
                    }

                // Inject events
                currentWeatherOnlyForecast = hourlyForecasts
                val combinedForecast = injectEvents(hourlyForecasts)

                // Logika rekomendasi (bisa kamu ganti dengan engine AI nantinya)
                val newRecommendation = "Pastikan semua cucian disimpan dalam ruang tertutup atau diberi pelindung."

                // Extract Sunrise & Sunset
                var formattedSunrise: String? = null
                var formattedSunset: String? = null
                if (dailyResponse.forecastDays.isNotEmpty()) {
                    val sunEvents = dailyResponse.forecastDays[0].sunEvents
                    // Assumed format from API: "2024-03-20T06:12:00" or similar.
                    // Let's extract the time part (HH:mm)
                    fun parseSunTime(timeStr: String): String {
                        return try {
                            val timePart = timeStr.substringAfter("T").take(5)
                            timePart // "HH:mm"
                        } catch (e: Exception) {
                            "00:00"
                        }
                    }
                    formattedSunrise = parseSunTime(sunEvents.sunriseTime)
                    formattedSunset = parseSunTime(sunEvents.sunsetTime)
                }

                sharedPreferences.edit().apply {
                    putLong("LAST_FETCH_TIME", currentTime)
                    putFloat("LAST_LAT", lat.toFloat())
                    putFloat("LAST_LON", lon.toFloat())
                    putString("WEATHER_DESC", weatherResponse.weatherCondition.description.text)
                    putString("TEMP", "${weatherResponse.temperature.degrees.roundToInt()}°C")
                    putString("ICON_URL", "${weatherResponse.weatherCondition.iconBaseUri}.png")
                    putString("LOCATION_NAME", addressText)
                    putString("RECOMMENDATION", newRecommendation)
                    putString("HOURLY_JSON", gson.toJson(hourlyForecasts)) // Keep weather-only JSON for cleaner caching

                    putString("HUMIDITY", "${weatherResponse.relativeHumidity}%")
                    putString("UV_INDEX", "${weatherResponse.uvIndex}")
                    putString("PRECIP_PROB", "${weatherResponse.precipitation.probability.percent}%")
                    putString("WIND_SPEED", "${weatherResponse.wind.speed.value.roundToInt()} km/h")
                    putString("FEELS_LIKE", "${weatherResponse.feelsLikeTemperature.degrees.roundToInt()}°C")
                    putString("THUNDER_PROB", "${weatherResponse.thunderstormProbability}%")
                    putString("WIND_DIRECTION", "${weatherResponse.wind.direction.cardinal} (${weatherResponse.wind.direction.degrees}°)")

                    putString("SUNRISE_TIME", formattedSunrise)
                    putString("SUNSET_TIME", formattedSunset)

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
                        hourlyForecasts = combinedForecast,
                        humidity = "${weatherResponse.relativeHumidity}%",
                        uvIndex = "${weatherResponse.uvIndex}",
                        precipitationProb = "${weatherResponse.precipitation.probability.percent}%",
                        windSpeed = "${weatherResponse.wind.speed.value.roundToInt()} km/h",
                        feelsLike = "${weatherResponse.feelsLikeTemperature.degrees.roundToInt()}°C",
                        thunderstormProb = "${weatherResponse.thunderstormProbability}%",
                        windDirection = "${weatherResponse.wind.direction.cardinal} (${weatherResponse.wind.direction.degrees}°)"
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