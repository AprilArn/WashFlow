package com.aprilarn.washflow.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NetworkMonitorLogicTest {

    private fun baseSnapshot(
        hasActiveNetwork: Boolean = true,
        hasInternetCapability: Boolean = true,
        isValidated: Boolean = true,
        downstreamKbps: Int = 50_000,
        upstreamKbps: Int = 20_000,
        wifiSignalStrengthDbm: Int? = -55
    ) = PassiveCapabilitiesSnapshot(
        hasActiveNetwork = hasActiveNetwork,
        hasInternetCapability = hasInternetCapability,
        isValidated = isValidated,
        downstreamKbps = downstreamKbps,
        upstreamKbps = upstreamKbps,
        wifiSignalStrengthDbm = wifiSignalStrengthDbm
    )

    @Test
    fun evaluatePassive_returnsOffline_whenNoActiveNetwork() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            PassiveCapabilitiesSnapshot.OFFLINE
        )
        assertEquals(NetworkStatus.Offline, status)
    }

    @Test
    fun evaluatePassive_returnsOffline_whenMissingInternetCapability() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            baseSnapshot(hasInternetCapability = false)
        )
        assertEquals(NetworkStatus.Offline, status)
    }

    @Test
    fun evaluatePassive_returnsNoInternet_whenNotValidated() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            baseSnapshot(isValidated = false)
        )
        assertEquals(NetworkStatus.NoInternet, status)
    }

    @Test
    fun evaluatePassive_returnsPoorConnection_whenDownstreamBandwidthIsLow() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            baseSnapshot(downstreamKbps = 250)
        )
        assertEquals(NetworkStatus.PoorConnection, status)
    }

    @Test
    fun evaluatePassive_returnsPoorConnection_whenUpstreamBandwidthIsLow() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            baseSnapshot(upstreamKbps = 100)
        )
        assertEquals(NetworkStatus.PoorConnection, status)
    }

    @Test
    fun evaluatePassive_returnsPoorConnection_whenWifiSignalIsWeak() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            baseSnapshot(wifiSignalStrengthDbm = -85)
        )
        assertEquals(NetworkStatus.PoorConnection, status)
    }

    @Test
    fun evaluatePassive_returnsConnected_whenAllCapabilitiesAreHealthy() {
        val status = NetworkMonitorClassifier.evaluatePassiveCapabilities(
            baseSnapshot()
        )
        assertEquals(NetworkStatus.Connected, status)
    }

    @Test
    fun classifyWithProbe_returnsConnected_whenProbeFastEvenIfNotYetValidated() {
        val snapshot = baseSnapshot(isValidated = false)
        val status = NetworkMonitorClassifier.classifyWithProbe(
            passiveStatus = NetworkStatus.NoInternet,
            snapshot = snapshot,
            probeResult = ProbeResult.Reachable(latencyMs = 180L),
            consecutiveFailures = 0
        )
        assertEquals(NetworkStatus.Connected, status)
    }

    @Test
    fun classifyWithProbe_returnsPoorConnection_whenProbeLatencyIsHigh() {
        val snapshot = baseSnapshot()
        val status = NetworkMonitorClassifier.classifyWithProbe(
            passiveStatus = NetworkStatus.Connected,
            snapshot = snapshot,
            probeResult = ProbeResult.Reachable(latencyMs = 1800L),
            consecutiveFailures = 0
        )
        assertEquals(NetworkStatus.PoorConnection, status)
    }

    @Test
    fun classifyWithProbe_returnsPoorConnection_whenWifiSignalIsCriticallyWeakDespiteFastProbe() {
        val snapshot = baseSnapshot(wifiSignalStrengthDbm = -90)
        val status = NetworkMonitorClassifier.classifyWithProbe(
            passiveStatus = NetworkStatus.PoorConnection,
            snapshot = snapshot,
            probeResult = ProbeResult.Reachable(latencyMs = 300L),
            consecutiveFailures = 0
        )
        assertEquals(NetworkStatus.PoorConnection, status)
    }

    @Test
    fun classifyWithProbe_returnsPoorConnection_onFirstTransientFailureWhileValidated() {
        val snapshot = baseSnapshot(isValidated = true)
        val status = NetworkMonitorClassifier.classifyWithProbe(
            passiveStatus = NetworkStatus.Connected,
            snapshot = snapshot,
            probeResult = ProbeResult.Unreachable,
            consecutiveFailures = 1
        )
        assertEquals(NetworkStatus.PoorConnection, status)
    }

    @Test
    fun classifyWithProbe_returnsNoInternet_onConsecutiveFailuresWhileValidated() {
        val snapshot = baseSnapshot(isValidated = true)
        val status = NetworkMonitorClassifier.classifyWithProbe(
            passiveStatus = NetworkStatus.Connected,
            snapshot = snapshot,
            probeResult = ProbeResult.Unreachable,
            consecutiveFailures = 2
        )
        assertEquals(NetworkStatus.NoInternet, status)
    }

    @Test
    fun classifyWithProbe_returnsNoInternet_immediatelyWhenUnvalidatedAndProbeFails() {
        val snapshot = baseSnapshot(isValidated = false)
        val status = NetworkMonitorClassifier.classifyWithProbe(
            passiveStatus = NetworkStatus.NoInternet,
            snapshot = snapshot,
            probeResult = ProbeResult.Unreachable,
            consecutiveFailures = 1
        )
        assertEquals(NetworkStatus.NoInternet, status)
    }

    @Test
    fun meaningfulKey_ignoresMinorHealthyRssiFluctuations() {
        val strongA = baseSnapshot(wifiSignalStrengthDbm = -58).toMeaningfulKey()
        val strongB = baseSnapshot(wifiSignalStrengthDbm = -64).toMeaningfulKey()
        val weak = baseSnapshot(wifiSignalStrengthDbm = -85).toMeaningfulKey()

        assertEquals(strongA, strongB)
        assertNotEquals(strongA, weak)
    }
}
