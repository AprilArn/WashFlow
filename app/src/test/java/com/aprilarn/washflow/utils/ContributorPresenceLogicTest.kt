package com.aprilarn.washflow.utils

import com.aprilarn.washflow.ui.feature.contributors.ContributorUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContributorPresenceLogicTest {

    @Test
    fun multiDeviceSessions_remainsOnlineWhenOneOfTwoDevicesLeaves() {
        val now = 1_000_000L
        val bothOnline = mapOf(
            "d_tablet" to (now - 10_000L),
            "d_phone" to (now - 5_000L)
        )
        assertEquals(2, ContributorPresenceRules.countActiveDevices(bothOnline, nowMillis = now))
        assertTrue(ContributorPresenceRules.isUserOnline(bothOnline, nowMillis = now))

        // Device B keluar -> hanya key d_phone dihapus, d_tablet masih aktif
        val onlyTabletOnline = mapOf("d_tablet" to (now - 10_000L))
        assertEquals(1, ContributorPresenceRules.countActiveDevices(onlyTabletOnline, nowMillis = now))
        assertTrue(ContributorPresenceRules.isUserOnline(onlyTabletOnline, nowMillis = now))

        // Semua device keluar -> map kosong -> langsung offline meski legacy flag lama sempat true
        assertEquals(
            0,
            ContributorPresenceRules.countActiveDevices(
                activeSessionsMillis = emptyMap(),
                legacyIsOnline = true,
                legacyLastActiveAtMillis = now,
                nowMillis = now
            )
        )
        assertFalse(
            ContributorPresenceRules.isUserOnline(
                activeSessionsMillis = emptyMap(),
                legacyIsOnline = true,
                legacyLastActiveAtMillis = now,
                nowMillis = now
            )
        )
    }

    @Test
    fun multiDeviceSessions_ignoresTimedOutDeviceWhileCountingFreshDevice() {
        val now = 1_000_000L
        val sessions = mapOf(
            "d_fresh" to (now - 30_000L),
            "d_crashed" to (now - ContributorPresenceRules.ONLINE_TIMEOUT_MS - 5_000L),
            "d_skewed" to (now + 5_000L) // toleransi clock skew minor
        )
        assertEquals(2, ContributorPresenceRules.countActiveDevices(sessions, nowMillis = now))
        assertTrue(ContributorPresenceRules.isUserOnline(sessions, nowMillis = now))
    }

    @Test
    fun multiDeviceSessions_offlineWhenAllDevicesTimedOut() {
        val now = 1_000_000L
        val staleSessions = mapOf(
            "d_1" to (now - ContributorPresenceRules.ONLINE_TIMEOUT_MS - 1L),
            "d_2" to (now - ContributorPresenceRules.ONLINE_TIMEOUT_MS - 60_000L)
        )
        assertEquals(0, ContributorPresenceRules.countActiveDevices(staleSessions, nowMillis = now))
        assertFalse(ContributorPresenceRules.isUserOnline(staleSessions, nowMillis = now))
    }

    @Test
    fun legacyFallback_worksWhenActiveSessionsFieldIsNull() {
        val now = 1_000_000L
        assertEquals(1, ContributorPresenceRules.countActiveDevices(null, legacyIsOnline = true, legacyLastActiveAtMillis = now - 30_000L, nowMillis = now))
        assertEquals(0, ContributorPresenceRules.countActiveDevices(null, legacyIsOnline = false, legacyLastActiveAtMillis = now - 1_000L, nowMillis = now))
        assertEquals(0, ContributorPresenceRules.countActiveDevices(null, legacyIsOnline = true, legacyLastActiveAtMillis = null, nowMillis = now))
        assertEquals(0, ContributorPresenceRules.countActiveDevices(null, legacyIsOnline = true, legacyLastActiveAtMillis = now - ContributorPresenceRules.ONLINE_TIMEOUT_MS - 1L, nowMillis = now))
    }

    @Test
    fun findStaleSessionIds_returnsOnlySessionsOlderThan24Hours() {
        val now = 100_000_000L
        val sessions = mapOf(
            "d_active" to (now - 10_000L),
            "d_recent_offline" to (now - 3_600_000L),
            "d_very_old" to (now - ContributorPresenceRules.STALE_SESSION_CLEANUP_MS - 1_000L)
        )
        assertEquals(listOf("d_very_old"), ContributorPresenceRules.findStaleSessionIds(sessions, now))
    }

    @Test
    fun sortContributors_placesOnlineAboveOfflineThenOwnerThenAlphabetical() {
        val offlineOwner = ContributorUiModel("1", "Alpha Owner", "a@test.com", null, "owner", isOnline = false, activeDeviceCount = 0)
        val onlineMemberZ = ContributorUiModel("2", "Zara Member", "z@test.com", null, "member", isOnline = true, activeDeviceCount = 1)
        val onlineMemberB = ContributorUiModel("3", "Budi Member", "b@test.com", null, "member", isOnline = true, activeDeviceCount = 2)
        val onlineOwner = ContributorUiModel("4", "Citra Owner", "c@test.com", null, "owner", isOnline = true, activeDeviceCount = 1)
        val offlineMemberA = ContributorUiModel("5", "Andi Member", "an@test.com", null, "member", isOnline = false, activeDeviceCount = 0)

        val sorted = ContributorPresenceRules.sortContributors(
            listOf(offlineOwner, onlineMemberZ, offlineMemberA, onlineMemberB, onlineOwner)
        )

        assertEquals(
            listOf("4", "3", "2", "1", "5"),
            sorted.map { it.uid }
        )
    }
}
