package com.aprilarn.washflow.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class NetworkStatus {
    Connected,
    PoorConnection,
    NoInternet,
    Offline
}

internal data class PassiveCapabilitiesSnapshot(
    val hasActiveNetwork: Boolean,
    val hasInternetCapability: Boolean,
    val isValidated: Boolean,
    val downstreamKbps: Int,
    val upstreamKbps: Int,
    val wifiSignalStrengthDbm: Int?
) {
    /**
     * Quantized key used with [distinctUntilChanged] so minor RSSI or bandwidth fluctuations
     * that do not cross any classification threshold do not trigger unnecessary HTTP probes.
     */
    fun toMeaningfulKey(): MeaningfulCapabilitiesKey {
        return MeaningfulCapabilitiesKey(
            hasActiveNetwork = hasActiveNetwork,
            hasInternetCapability = hasInternetCapability,
            isValidated = isValidated,
            isLowBandwidth = NetworkMonitorClassifier.isLowBandwidth(downstreamKbps, upstreamKbps),
            isPoorWifiSignal = NetworkMonitorClassifier.isPoorWifiSignal(wifiSignalStrengthDbm),
            isExtremeWifiSignal = NetworkMonitorClassifier.isExtremeWifiSignal(wifiSignalStrengthDbm)
        )
    }

    companion object {
        val OFFLINE = PassiveCapabilitiesSnapshot(
            hasActiveNetwork = false,
            hasInternetCapability = false,
            isValidated = false,
            downstreamKbps = 0,
            upstreamKbps = 0,
            wifiSignalStrengthDbm = null
        )
    }
}

internal data class MeaningfulCapabilitiesKey(
    val hasActiveNetwork: Boolean,
    val hasInternetCapability: Boolean,
    val isValidated: Boolean,
    val isLowBandwidth: Boolean,
    val isPoorWifiSignal: Boolean,
    val isExtremeWifiSignal: Boolean
)

internal sealed interface ProbeResult {
    data class Reachable(val latencyMs: Long) : ProbeResult
    data object Unreachable : ProbeResult
}

internal object NetworkMonitorClassifier {
    const val POOR_DOWNSTREAM_KBPS_THRESHOLD = 500
    const val POOR_UPSTREAM_KBPS_THRESHOLD = 200
    const val POOR_WIFI_SIGNAL_DBM_THRESHOLD = -82
    const val EXTREME_WIFI_SIGNAL_DBM_THRESHOLD = -86
    const val POOR_LATENCY_MS_THRESHOLD = 1500L
    const val CONSECUTIVE_FAILURE_THRESHOLD = 2

    fun isLowBandwidth(downstreamKbps: Int, upstreamKbps: Int): Boolean {
        val lowDown = downstreamKbps in 1 until POOR_DOWNSTREAM_KBPS_THRESHOLD
        val lowUp = upstreamKbps in 1 until POOR_UPSTREAM_KBPS_THRESHOLD
        return lowDown || lowUp
    }

    fun isPoorWifiSignal(wifiSignalStrengthDbm: Int?): Boolean {
        return wifiSignalStrengthDbm != null && wifiSignalStrengthDbm < POOR_WIFI_SIGNAL_DBM_THRESHOLD
    }

    fun isExtremeWifiSignal(wifiSignalStrengthDbm: Int?): Boolean {
        return wifiSignalStrengthDbm != null && wifiSignalStrengthDbm < EXTREME_WIFI_SIGNAL_DBM_THRESHOLD
    }

    fun evaluatePassiveCapabilities(snapshot: PassiveCapabilitiesSnapshot): NetworkStatus {
        if (!snapshot.hasActiveNetwork || !snapshot.hasInternetCapability) {
            return NetworkStatus.Offline
        }
        if (!snapshot.isValidated) {
            return NetworkStatus.NoInternet
        }
        if (isPoorWifiSignal(snapshot.wifiSignalStrengthDbm) ||
            isLowBandwidth(snapshot.downstreamKbps, snapshot.upstreamKbps)
        ) {
            return NetworkStatus.PoorConnection
        }
        return NetworkStatus.Connected
    }

    fun classifyWithProbe(
        passiveStatus: NetworkStatus,
        snapshot: PassiveCapabilitiesSnapshot,
        probeResult: ProbeResult,
        consecutiveFailures: Int
    ): NetworkStatus {
        if (passiveStatus == NetworkStatus.Offline) {
            return NetworkStatus.Offline
        }

        return when (probeResult) {
            is ProbeResult.Reachable -> {
                if (probeResult.latencyMs >= POOR_LATENCY_MS_THRESHOLD ||
                    isExtremeWifiSignal(snapshot.wifiSignalStrengthDbm)
                ) {
                    NetworkStatus.PoorConnection
                } else {
                    NetworkStatus.Connected
                }
            }
            ProbeResult.Unreachable -> {
                when {
                    !snapshot.isValidated -> NetworkStatus.NoInternet
                    consecutiveFailures >= CONSECUTIVE_FAILURE_THRESHOLD -> NetworkStatus.NoInternet
                    else -> NetworkStatus.PoorConnection
                }
            }
        }
    }
}

/**
 * Hybrid network monitor combining passive [ConnectivityManager.registerDefaultNetworkCallback]
 * observation with a lightweight, foreground-only HTTP latency/reachability probe.
 *
 * Collect via `collectAsStateWithLifecycle()` so monitoring and probing automatically pause
 * when the app moves to the background.
 */
class NetworkMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @OptIn(ExperimentalCoroutinesApi::class)
    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val passiveState = MutableStateFlow(readActiveSnapshot())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                passiveState.value = readSnapshotForNetwork(network)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                passiveState.value = buildSnapshot(networkCapabilities)
            }

            override fun onLost(network: Network) {
                val active = connectivityManager.activeNetwork
                passiveState.value = if (active == null || active == network) {
                    PassiveCapabilitiesSnapshot.OFFLINE
                } else {
                    readSnapshotForNetwork(active)
                }
            }
        }

        val registered = try {
            connectivityManager.registerDefaultNetworkCallback(callback)
            true
        } catch (_: Exception) {
            false
        }

        val probeJob = launch {
            passiveState
                .distinctUntilChanged { old, new -> old.toMeaningfulKey() == new.toMeaningfulKey() }
                .collectLatest { snapshot ->
                    val passiveStatus = NetworkMonitorClassifier.evaluatePassiveCapabilities(snapshot)

                    if (passiveStatus == NetworkStatus.Offline) {
                        trySend(NetworkStatus.Offline)
                        return@collectLatest
                    }

                    // Emit passive estimate immediately while the first probe runs
                    trySend(passiveStatus)

                    var consecutiveFailures = 0
                    while (currentCoroutineContext().isActive) {
                        val probeResult = runReachabilityProbe()
                        consecutiveFailures = when (probeResult) {
                            is ProbeResult.Reachable -> 0
                            ProbeResult.Unreachable -> consecutiveFailures + 1
                        }

                        val resolvedStatus = NetworkMonitorClassifier.classifyWithProbe(
                            passiveStatus = passiveStatus,
                            snapshot = snapshot,
                            probeResult = probeResult,
                            consecutiveFailures = consecutiveFailures
                        )
                        trySend(resolvedStatus)

                        val nextIntervalMs = if (resolvedStatus == NetworkStatus.Connected) {
                            CONNECTED_POLL_INTERVAL_MS
                        } else {
                            DEGRADED_POLL_INTERVAL_MS
                        }
                        delay(nextIntervalMs)
                    }
                }
        }

        awaitClose {
            probeJob.cancel()
            if (registered) {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (_: Exception) {
                }
            }
        }
    }
        .distinctUntilChanged()
        .transformLatest { status ->
            if (status == NetworkStatus.Connected) {
                emit(status)
            } else {
                delay(DEGRADE_DEBOUNCE_MS)
                emit(status)
            }
        }
        .distinctUntilChanged()

    private fun readActiveSnapshot(): PassiveCapabilitiesSnapshot {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
                ?: return PassiveCapabilitiesSnapshot.OFFLINE
            readSnapshotForNetwork(activeNetwork)
        } catch (_: Exception) {
            PassiveCapabilitiesSnapshot.OFFLINE
        }
    }

    private fun readSnapshotForNetwork(network: Network): PassiveCapabilitiesSnapshot {
        val caps = try {
            connectivityManager.getNetworkCapabilities(network)
        } catch (_: Exception) {
            null
        } ?: return PassiveCapabilitiesSnapshot.OFFLINE
        return buildSnapshot(caps)
    }

    private fun buildSnapshot(capabilities: NetworkCapabilities): PassiveCapabilitiesSnapshot {
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val wifiSignalDbm = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        ) {
            val strength = capabilities.signalStrength
            if (strength != NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED) strength else null
        } else {
            null
        }

        return PassiveCapabilitiesSnapshot(
            hasActiveNetwork = true,
            hasInternetCapability = hasInternet,
            isValidated = isValidated,
            downstreamKbps = capabilities.linkDownstreamBandwidthKbps,
            upstreamKbps = capabilities.linkUpstreamBandwidthKbps,
            wifiSignalStrengthDbm = wifiSignalDbm
        )
    }

    private suspend fun runReachabilityProbe(): ProbeResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val startMs = SystemClock.elapsedRealtime()
            connection = (URL(PROBE_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                connectTimeout = PROBE_TIMEOUT_MS
                readTimeout = PROBE_TIMEOUT_MS
                instanceFollowRedirects = false
                useCaches = false
            }
            val code = connection.responseCode
            val elapsedMs = SystemClock.elapsedRealtime() - startMs
            if (code in 200..299) {
                ProbeResult.Reachable(latencyMs = elapsedMs)
            } else {
                ProbeResult.Unreachable
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            ProbeResult.Unreachable
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        private const val PROBE_URL = "https://connectivitycheck.gstatic.com/generate_204"
        private const val PROBE_TIMEOUT_MS = 3500
        private const val DEGRADE_DEBOUNCE_MS = 1200L
        private const val CONNECTED_POLL_INTERVAL_MS = 30_000L
        private const val DEGRADED_POLL_INTERVAL_MS = 10_000L
    }
}
