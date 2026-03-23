// com/aprilarn/washflow/ui/settings/SettingsViewModel.kt
package com.aprilarn.washflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.BuildConfig
import com.aprilarn.washflow.data.remote.weather.service.GeocodingApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SettingsViewModel(
    private val geocodingApiService: GeocodingApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    // Fungsi ini dipanggil dari navigation di MainActivity
    fun fetchAddressAndSave(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Set sementara saat loading
            _uiState.update { it.copy(locationName = "Mencari lokasi...", latitude = lat, longitude = lon) }

            try {
                // Tembak API Google Geocoding
                // Gunakan BuildConfig.API_KEY yang sudah kita set sebelumnya
                val response = geocodingApiService.getAddressFromLocation(
                    latlng = "$lat,$lon",
                    apiKey = BuildConfig.API_KEY
                )

                if (response.status == "OK" && response.results.isNotEmpty()) {
                    // Ambil address pertama (biasanya paling spesifik)
                    val fullAddress = response.results[0].formattedAddress

                    // Kita rapikan sedikit agar tidak kepanjangan (misal hapus Kode Pos/Negara jika ada)
                    // Contoh rapikan sederhana: "Boyolali, Jawa Tengah 57312, Indonesia" -> "Boyolali, Jawa Tengah"
                    val cleanedAddress = fullAddress
                        .split(",") // Pecah berdasarkan koma
                        .take(2)    // Ambil 2 bagian pertama saja (Kota/Kab + Provinsi)
                        .joinToString(", ") // Gabungkan kembali dengan koma
                        .trim()

                    _uiState.update { it.copy(locationName = cleanedAddress) }
                } else {
                    // Fallback jika API OK tapi results kosong
                    _uiState.update { it.copy(locationName = "Pilih lokasi") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback jika network error
                _uiState.update { it.copy(locationName = "Gagal mengambil nama tempat") }
            }
        }
    }

    // --- FACTORY untuk Manual Injection tanpa Hilt ---
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // Inisialisasi Retrofit khusus untuk Google Maps API
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/") // Base URL Google Maps
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(GeocodingApiService::class.java)
                return SettingsViewModel(service) as T
            }
        }
    }
}