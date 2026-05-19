package com.aprilarn.washflow.data.remote.weather.service

import com.aprilarn.washflow.BuildConfig
import com.aprilarn.washflow.BuildConfig.API_KEY
import com.aprilarn.washflow.data.remote.weather.api.GoogleCurrentWeatherResponse
import com.aprilarn.washflow.data.remote.weather.api.GoogleDailyForecastResponse
import com.aprilarn.washflow.data.remote.weather.api.GoogleHourlyForecastResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApiService {

    // Mengarah ke endpoint currentConditions:lookup
    @GET("v1/currentConditions:lookup")
    suspend fun getCurrentConditions(
        @Query("location.latitude") lat: Double,
        @Query("location.longitude") lon: Double,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("languageCode") lang: String = "en"
    ): GoogleCurrentWeatherResponse

    // Tambahkan "v1/" di awal path
    @GET("v1/forecast/hours:lookup")
    suspend fun getHourlyForecastData(
        @Query("location.latitude") lat: Double,
        @Query("location.longitude") lon: Double,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("hours") hours: Int = 8,
        @Query("languageCode") lang: String = "en"
    ): GoogleHourlyForecastResponse

    @GET("v1/forecast/days:lookup")
    suspend fun getDailyForecastData(
        @Query("location.latitude") lat: Double,
        @Query("location.longitude") lon: Double,
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("days") days: Int = 1,
        @Query("languageCode") lang: String = "en"
    ): GoogleDailyForecastResponse

    companion object {
        @JvmStatic
        operator fun invoke(): WeatherApiService {
            val okHttpClient =
                OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()
            val retrofit =
                Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL) // Pastikan BASE_URL di build.gradle sudah benar
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            return retrofit.create(WeatherApiService::class.java)
        }
    }
}