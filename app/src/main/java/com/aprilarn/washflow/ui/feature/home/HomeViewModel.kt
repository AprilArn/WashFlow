package com.aprilarn.washflow.ui.feature.home

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.BuildConfig
import com.aprilarn.washflow.R
import com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService
import com.aprilarn.washflow.data.remote.weather.service.WeatherApiService
import com.aprilarn.washflow.domain.usecase.ManageOrderUseCase
import com.aprilarn.washflow.domain.usecase.ManageWorkspaceUseCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeViewModel(
    private val orderUseCase: ManageOrderUseCase,
    private val workspaceUseCase: ManageWorkspaceUseCase,
    private val geocodingApiService: GeocodingApiService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var currentWeatherOnlyForecast: List<HourlyForecastUiState> = emptyList()
    private var todayDeadlines: List<HourlyForecastUiState> = emptyList()

    var hasInitializedWeather: Boolean = false
        private set

    private val weatherApiService = WeatherApiService()
    private val gson = Gson()

    init {
        val cachedWeather = sharedPreferences.getString("WEATHER_DESC", null)
        val cachedWeatherType = sharedPreferences.getString("WEATHER_TYPE", "") ?: ""
        val cachedIsDaytime = if (sharedPreferences.contains("IS_DAYTIME")) {
            sharedPreferences.getBoolean("IS_DAYTIME", true)
        } else {
            null
        }
        if (!cachedWeather.isNullOrBlank()) {
            val savedTemp = sharedPreferences.getString("TEMP", "--°C") ?: "--°C"
            val savedIcon = sharedPreferences.getString("ICON_URL", "") ?: ""
            val rawLocName = sharedPreferences.getString("LOCATION_NAME", null)
            val validLocName = rawLocName?.takeIf {
                it.isNotBlank() &&
                    !it.equals("Mencari lokasi...", ignoreCase = true) &&
                    !it.equals("Lokasi tidak diketahui", ignoreCase = true) &&
                    !it.equals("Gagal memuat alamat", ignoreCase = true) &&
                    !it.equals("Unknown Location", ignoreCase = true)
            }
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
            } catch (e: Exception) {
                emptyList()
            }
            val nowMillis = System.currentTimeMillis()
            val filteredHourly = savedHourly.filter { it.timestamp > nowMillis }.take(8)
            currentWeatherOnlyForecast = filteredHourly

            _uiState.update {
                it.copy(
                    greeting = getGreetingMessage(),
                    greetingResId = getGreetingResId(),
                    weather = cachedWeather,
                    weatherFallbackResId = null,
                    weatherType = cachedWeatherType,
                    isDaytime = cachedIsDaytime,
                    temperature = savedTemp,
                    weatherIconUrl = savedIcon,
                    locationName = validLocName ?: "",
                    locationNameResId = if (validLocName != null) null else R.string.home_location_unknown,
                    recommendation = "",
                    recommendationResId = R.string.home_recommendation_default,
                    hourlyForecasts = injectEvents(filteredHourly),
                    humidity = savedHumidity,
                    uvIndex = savedUV,
                    precipitationProb = savedPrecip,
                    windSpeed = savedWind,
                    feelsLike = savedFeels,
                    thunderstormProb = savedThunder,
                    windDirection = savedWindDir
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    greeting = getGreetingMessage(),
                    greetingResId = getGreetingResId(),
                    isDaytime = cachedIsDaytime
                )
            }
        }

        // Lakukan sinkronisasi penuh satu kali saat aplikasi/Home dibuka
        // Untuk memastikan data tidak out-of-sync akibat perubahan manual di database
        viewModelScope.launch {
            val workspaceId = workspaceUseCase.getCurrentWorkspaceId()
            if (workspaceId != null) {
                workspaceUseCase.syncMetadata(workspaceId)
            }
        }
        
        listenForMetadataUpdates()
        listenForDeadlineUpdates()
        startPeriodicRefresh()
    }

    private fun listenForMetadataUpdates() {
        viewModelScope.launch {
            workspaceUseCase.observeMetadata()
                .catch { e ->
                    Log.e("HomeViewModel", "Error listening for metadata updates", e)
                }
                .collect { metadata ->
                    // Jika total order > 0 tapi rincian status masih 0 semua, berarti perlu sync (migrasi data lama)
                    val totalStatusCount = metadata.orderOnQueueCount + metadata.orderOnProcessCount + metadata.orderDoneCount
                    
                    val workspaceId = workspaceUseCase.getCurrentWorkspaceId()
                    if (workspaceId != null) {
                        // Sinkronisasi otomatis jika data tidak seimbang, minus, atau indikasi manipulasi console
                        val isMinus = metadata.orderCount < 0 || metadata.orderDoneCount < 0 || metadata.orderOnProcessCount < 0 || metadata.orderOnQueueCount < 0
                        val isUnbalanced = metadata.orderCount != totalStatusCount
                        
                        if (isMinus || isUnbalanced) {
                            workspaceUseCase.syncMetadata(workspaceId)
                        }
                    }

                    _uiState.update {
                        it.copy(
                            inQueue = metadata.orderOnQueueCount,
                            onProcess = metadata.orderOnProcessCount,
                            done = metadata.orderDoneCount
                        )
                    }
                }
        }
    }

    private fun listenForDeadlineUpdates() {
        viewModelScope.launch {
            orderUseCase.observeActiveOrdersWithDeadlines()
                .catch { e ->
                    Log.e("HomeViewModel", "Error listening for deadline updates", e)
                }
                .collect { orders ->
                    // Filter for deadlines that fall within our forecast window
                    val now = Calendar.getInstance()
                    val windowEnd = Calendar.getInstance().apply {
                        add(Calendar.HOUR_OF_DAY, 12) // Buffer matching weather fetch
                    }

                    todayDeadlines = orders.filter { order ->
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
                    fetchWeatherData(lastLat, lastLon, isGps, showLoader = false)
                } else {
                    _uiState.update {
                        it.copy(
                            greeting = getGreetingMessage(),
                            greetingResId = getGreetingResId()
                        )
                    }
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

    private fun getGreetingResId(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> R.string.home_greeting_morning
            in 11..15 -> R.string.home_greeting_afternoon
            in 16..20 -> R.string.home_greeting_evening
            else -> R.string.home_greeting_night
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

    private fun parseIsoToTimestamp(isoString: String): Long {
        return try {
            // Contoh input API: "2025-02-10T15:02:35.703929582Z"

            // 1. Buang bagian pecahan detik dan huruf Z agar sesuai dengan format SimpleDateFormat
            val cleanString = isoString.substringBefore(".")

            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

            // 2. Beritahu sistem bahwa string ini adalah waktu UTC
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")

            format.parse(cleanString)?.time ?: 0L
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Gagal parse waktu: $isoString", e)
            0L
        }
    }

    private fun formatTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date(timestamp))
    }

    private fun injectEvents(
        weatherForecasts: List<HourlyForecastUiState>
    ): List<HourlyForecastUiState> {
        val result = weatherForecasts.toMutableList()

        val startTs = System.currentTimeMillis()
        val endTs = startTs + (12 * 60 * 60 * 1000L) // Window 12 jam kedepan dari sekarang

        val openTime = sharedPreferences.getString("WS_OPEN_TIME", null)
        val closeTime = sharedPreferences.getString("WS_CLOSE_TIME", null)

        val sunriseJson = sharedPreferences.getString("SUNRISE_TIMESTAMPS", "[]")
        val sunsetJson = sharedPreferences.getString("SUNSET_TIMESTAMPS", "[]")
        val sunriseTimestamps: List<Long> = try {
            gson.fromJson(sunriseJson, object : TypeToken<List<Long>>() {}.type)
        } catch (e: Exception) { emptyList() }
        val sunsetTimestamps: List<Long> = try {
            gson.fromJson(sunsetJson, object : TypeToken<List<Long>>() {}.type)
        } catch (e: Exception) { emptyList() }

        fun getTimestampsInWindow(timeStr: String?): List<Long> {
            if (timeStr == null) return emptyList()
            val parts = timeStr.split(":").map { it.toIntOrNull() ?: 0 }
            val hour = if (parts.isNotEmpty()) parts[0] else 0
            val minute = if (parts.size >= 2) parts[1] else 0

            val results = mutableListOf<Long>()
            val baseCal = Calendar.getInstance().apply { timeInMillis = startTs }

            // Check yesterday, today, and tomorrow to be safe
            for (offset in -1..1) {
                val cal = (baseCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val ts = cal.timeInMillis
                if (ts in startTs..endTs) {
                    results.add(ts)
                }
            }
            return results
        }

        // Inject Shop Hours
        getTimestampsInWindow(openTime).forEach { ts ->
            result.add(HourlyForecastUiState(
                time = formatTimestampToTime(ts),
                iconUrl = "WS_OPEN",
                temperature = "--",
                isEvent = true,
                eventLabel = "Open",
                timestamp = ts
            ))
        }

        getTimestampsInWindow(closeTime).forEach { ts ->
            result.add(HourlyForecastUiState(
                time = formatTimestampToTime(ts),
                iconUrl = "WS_CLOSE",
                temperature = "--",
                isEvent = true,
                eventLabel = "Closed",
                timestamp = ts
            ))
        }

        // Inject Sunrise/Sunset
        sunriseTimestamps.forEach { ts ->
            if (ts in startTs..endTs) {
                result.add(HourlyForecastUiState(
                    time = formatTimestampToTime(ts),
                    iconUrl = "WS_SUNRISE",
                    temperature = "--",
                    isEvent = true,
                    eventLabel = "Sunrise",
                    timestamp = ts
                ))
            }
        }

        sunsetTimestamps.forEach { ts ->
            if (ts in startTs..endTs) {
                result.add(HourlyForecastUiState(
                    time = formatTimestampToTime(ts),
                    iconUrl = "WS_SUNSET",
                    temperature = "--",
                    isEvent = true,
                    eventLabel = "Sunset",
                    timestamp = ts
                ))
            }
        }

        // Add deadlines that are within the forecast window
        todayDeadlines.forEach { deadline ->
            getTimestampsInWindow(deadline.time).forEach { ts ->
                result.add(deadline.copy(timestamp = ts))
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

        return sortedAll.take(8)
    }

    fun fetchWeatherData(lat: Double, lon: Double, isGps: Boolean = true, showLoader: Boolean = true) {
        hasInitializedWeather = true
        val currentTime = System.currentTimeMillis()
        val lastFetchTime = sharedPreferences.getLong("LAST_FETCH_TIME", 0L)
        val lastLat = sharedPreferences.getFloat("LAST_LAT", 0f).toDouble()
        val lastLon = sharedPreferences.getFloat("LAST_LON", 0f).toDouble()

        val isSameLocation = abs(lastLat - lat) < 0.01 && abs(lastLon - lon) < 0.01

        val currentSlot = getTimestampSlot(currentTime)
        val lastSlot = getTimestampSlot(lastFetchTime)
        val activeLang = sharedPreferences.getString("APP_LANGUAGE", "en") ?: "en"
        val lastWeatherLang = sharedPreferences.getString("LAST_WEATHER_LANG", null)
        val isCacheValid = currentSlot == lastSlot && lastWeatherLang == activeLang

        if (isSameLocation && isCacheValid) {
            Log.d("HomeViewModel", "Menggunakan data cuaca dari Cache Memori")

            val savedWeather = sharedPreferences.getString("WEATHER_DESC", null)
            val savedWeatherType = sharedPreferences.getString("WEATHER_TYPE", "") ?: ""
            val savedIsDaytime = if (sharedPreferences.contains("IS_DAYTIME")) {
                sharedPreferences.getBoolean("IS_DAYTIME", true)
            } else {
                null
            }
            val savedTemp = sharedPreferences.getString("TEMP", "--°C") ?: "--°C"
            val savedIcon = sharedPreferences.getString("ICON_URL", "") ?: ""
            val rawLocName = sharedPreferences.getString("LOCATION_NAME", null)
            val validLocName = rawLocName?.takeIf {
                it.isNotBlank() &&
                    !it.equals("Mencari lokasi...", ignoreCase = true) &&
                    !it.equals("Lokasi tidak diketahui", ignoreCase = true) &&
                    !it.equals("Gagal memuat alamat", ignoreCase = true) &&
                    !it.equals("Unknown Location", ignoreCase = true)
            }

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
            }.take(8)

            // Inject operational hours into cached data as well
            currentWeatherOnlyForecast = filteredHourly
            val combinedForecast = injectEvents(filteredHourly)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    greeting = getGreetingMessage(),
                    greetingResId = getGreetingResId(),
                    weather = savedWeather ?: "",
                    weatherFallbackResId = if (savedWeather.isNullOrBlank()) R.string.common_unknown else null,
                    weatherType = savedWeatherType,
                    isDaytime = savedIsDaytime,
                    temperature = savedTemp,
                    weatherIconUrl = savedIcon,
                    locationName = validLocName ?: "",
                    locationNameResId = if (validLocName != null) null else R.string.home_location_unknown,
                    recommendation = "",
                    recommendationResId = R.string.home_recommendation_default,
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
        if (showLoader) {
            _uiState.update { it.copy(isLoading = true, isGpsLocation = isGps) }
        }

        viewModelScope.launch {
            try {
                val weatherResponse = weatherApiService.getCurrentConditions(lat = lat, lon = lon, lang = activeLang)
                val forecastResponse = weatherApiService.getHourlyForecastData(lat = lat, lon = lon, hours = 24, lang = activeLang)
                val dailyResponse = weatherApiService.getDailyForecastData(lat = lat, lon = lon, days = 2, lang = activeLang)

                val geoResponse = geocodingApiService.getAddressFromLocation("$lat,$lon", BuildConfig.API_KEY)
                var addressText: String? = null
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
                val currentTimeMillis = now.timeInMillis
                val locationTimeZone = try {
                    java.util.TimeZone.getTimeZone(forecastResponse.timeZone.id)
                } catch (e: Exception) {
                    java.util.TimeZone.getDefault()
                }

                val hourlyForecasts = forecastResponse.forecastHours
                    .map { forecastItem ->
                        val time = String.format(Locale.US, "%02d:00", forecastItem.displayDateTime.hours)
                        val iconUrl = "${forecastItem.weatherCondition.iconBaseUri}.png"

                        // Gunakan UTC timestamp dari interval.startTime agar akurat lintas zona waktu negara
                        val parsedStartTs = parseIsoToTimestamp(forecastItem.interval.startTime)
                        val itemTimestamp = if (parsedStartTs > 0L) {
                            parsedStartTs
                        } else {
                            Calendar.getInstance(locationTimeZone).apply {
                                set(Calendar.YEAR, forecastItem.displayDateTime.year)
                                set(Calendar.MONTH, forecastItem.displayDateTime.month - 1)
                                set(Calendar.DAY_OF_MONTH, forecastItem.displayDateTime.day)
                                set(Calendar.HOUR_OF_DAY, forecastItem.displayDateTime.hours)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        }

                        HourlyForecastUiState(
                            time = time,
                            iconUrl = iconUrl,
                            temperature = "${forecastItem.temperature.degrees.roundToInt()}°",
                            timestamp = itemTimestamp
                        )
                    }
                    .filter { it.timestamp > currentTimeMillis }
                    .take(8) // Limit to 8 items as required for the UI

                // Inject events
                currentWeatherOnlyForecast = hourlyForecasts
                val combinedForecast = injectEvents(hourlyForecasts)

                // Extract Sunrise & Sunset
                val sunriseTimestamps = mutableListOf<Long>()
                val sunsetTimestamps = mutableListOf<Long>()
                dailyResponse.forecastDays.forEach { day ->
                    val sunrise = parseIsoToTimestamp(day.sunEvents.sunriseTime)
                    val sunset = parseIsoToTimestamp(day.sunEvents.sunsetTime)
                    if (sunrise > 0L) sunriseTimestamps.add(sunrise)
                    if (sunset > 0L) sunsetTimestamps.add(sunset)
                }

                sharedPreferences.edit().apply {
                    putLong("LAST_FETCH_TIME", currentTime)
                    putString("LAST_WEATHER_LANG", activeLang)
                    putFloat("LAST_LAT", lat.toFloat())
                    putFloat("LAST_LON", lon.toFloat())
                    putString("WEATHER_DESC", weatherResponse.weatherCondition.description.text)
                    putString("WEATHER_TYPE", weatherResponse.weatherCondition.type)
                    putBoolean("IS_DAYTIME", weatherResponse.isDaytime)
                    putString("TEMP", "${weatherResponse.temperature.degrees.roundToInt()}°C")
                    putString("ICON_URL", "${weatherResponse.weatherCondition.iconBaseUri}.png")
                    if (addressText != null) {
                        putString("LOCATION_NAME", addressText)
                    } else {
                        remove("LOCATION_NAME")
                    }
                    remove("RECOMMENDATION")
                    putString("HOURLY_JSON", gson.toJson(hourlyForecasts)) // Keep weather-only JSON for cleaner caching

                    putString("HUMIDITY", "${weatherResponse.relativeHumidity}%")
                    putString("UV_INDEX", "${weatherResponse.uvIndex}")
                    putString("PRECIP_PROB", "${weatherResponse.precipitation.probability.percent}%")
                    putString("WIND_SPEED", "${weatherResponse.wind.speed.value.roundToInt()} km/h")
                    putString("FEELS_LIKE", "${weatherResponse.feelsLikeTemperature.degrees.roundToInt()}°C")
                    putString("THUNDER_PROB", "${weatherResponse.thunderstormProbability}%")
                    putString("WIND_DIRECTION", "${weatherResponse.wind.direction.cardinal} (${weatherResponse.wind.direction.degrees}°)")

                    putString("SUNRISE_TIMESTAMPS", gson.toJson(sunriseTimestamps))
                    putString("SUNSET_TIMESTAMPS", gson.toJson(sunsetTimestamps))

                    apply()
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        greeting = getGreetingMessage(),
                        greetingResId = getGreetingResId(),
                        weather = weatherResponse.weatherCondition.description.text,
                        weatherFallbackResId = null,
                        weatherType = weatherResponse.weatherCondition.type,
                        isDaytime = weatherResponse.isDaytime,
                        temperature = "${weatherResponse.temperature.degrees.roundToInt()}°C",
                        weatherIconUrl = "${weatherResponse.weatherCondition.iconBaseUri}.png",
                        locationName = addressText ?: "",
                        locationNameResId = if (addressText != null) null else R.string.home_location_unknown,
                        isGpsLocation = isGps,
                        recommendation = "",
                        recommendationResId = R.string.home_recommendation_default,
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
                        greetingResId = getGreetingResId(),
                        weather = "",
                        weatherFallbackResId = R.string.home_weather_failed,
                        locationName = "",
                        locationNameResId = R.string.home_location_failed,
                        recommendation = "",
                        recommendationResId = R.string.home_recommendation_failed
                    )
                }
            }
        }
    }
}