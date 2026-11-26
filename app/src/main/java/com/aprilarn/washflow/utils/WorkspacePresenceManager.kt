package com.aprilarn.washflow.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aprilarn.washflow.ui.feature.contributors.ContributorUiModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID

/**
 * Pure multi-device presence & sorting rules for workspace contributors (easily unit-testable).
 */
object ContributorPresenceRules {
    /** Interval heartbeat saat aplikasi aktif di foreground. */
    const val HEARTBEAT_INTERVAL_MS: Long = 45_000L

    /** Batas maksimal usia heartbeat sebelum sesi dianggap offline (mis. crash/putus jaringan). */
    const val ONLINE_TIMEOUT_MS: Long = 120_000L

    /** Batas usia sesi basi di map `activeSessions` untuk dibersihkan otomatis (24 jam). */
    const val STALE_SESSION_CLEANUP_MS: Long = 24L * 60L * 60L * 1000L

    /**
     * Menghitung jumlah perangkat yang sedang aktif untuk satu akun.
     * - Jika map [activeSessionsMillis] tersedia (meski kosong), evaluasi sepenuhnya per-perangkat.
     * - Jika null (akun masih memakai versi app lama), fallback ke [legacyIsOnline] & [legacyLastActiveAtMillis].
     */
    fun countActiveDevices(
        activeSessionsMillis: Map<String, Long>?,
        legacyIsOnline: Boolean = false,
        legacyLastActiveAtMillis: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Int {
        if (activeSessionsMillis != null) {
            return activeSessionsMillis.values.count { ts ->
                isTimestampFresh(ts, nowMillis)
            }
        }
        return if (legacyIsOnline && isTimestampFresh(legacyLastActiveAtMillis, nowMillis)) 1 else 0
    }

    fun isUserOnline(
        activeSessionsMillis: Map<String, Long>?,
        legacyIsOnline: Boolean = false,
        legacyLastActiveAtMillis: Long? = null,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return countActiveDevices(
            activeSessionsMillis = activeSessionsMillis,
            legacyIsOnline = legacyIsOnline,
            legacyLastActiveAtMillis = legacyLastActiveAtMillis,
            nowMillis = nowMillis
        ) > 0
    }

    /**
     * Mengembalikan daftar key `deviceId` yang sudah sangat basi (> 24 jam) agar bisa dibersihkan.
     */
    fun findStaleSessionIds(
        activeSessionsMillis: Map<String, Long>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<String> {
        return activeSessionsMillis.entries
            .filter { (_, ts) -> (nowMillis - ts) > STALE_SESSION_CLEANUP_MS }
            .map { it.key }
    }

    private fun isTimestampFresh(timestampMillis: Long?, nowMillis: Long): Boolean {
        if (timestampMillis == null) return false
        val ageMs = nowMillis - timestampMillis
        // Toleransi clock skew minor jika server timestamp sedikit lebih maju dari jam lokal
        return ageMs <= ONLINE_TIMEOUT_MS
    }

    /**
     * Mengurutkan kontributor:
     * 1. Online di atas, Offline di bawah
     * 2. Owner di atas Member dalam masing-masing grup
     * 3. Alfabetis berdasarkan nama
     */
    fun sortContributors(list: List<ContributorUiModel>): List<ContributorUiModel> {
        return list.sortedWith(
            compareBy<ContributorUiModel> { !it.isOnline }
                .thenBy { !it.role.equals("owner", ignoreCase = true) }
                .thenBy { it.name.lowercase(Locale.US) }
        )
    }
}

/**
 * Mengelola pembaruan status kehadiran per-perangkat (`activeSessions.<deviceId>`)
 * pada dokumen `users/{uid}` di Firestore agar login multi-device tidak saling menimpa.
 */
object WorkspacePresenceManager {
    private const val PREFS_NAME = "WashFlowPrefs"
    private const val KEY_DEVICE_ID = "PRESENCE_DEVICE_ID"

    private val usersCollection
        get() = Firebase.firestore.collection("users")

    fun getOrCreateDeviceId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return getOrCreateDeviceId(prefs)
    }

    fun getOrCreateDeviceId(sharedPreferences: SharedPreferences): String {
        val existing = sharedPreferences.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing
        val generated = "d_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        sharedPreferences.edit().putString(KEY_DEVICE_ID, generated).apply()
        return generated
    }

    fun setOnline(context: Context, uid: String) {
        setOnlineWithDeviceId(uid, getOrCreateDeviceId(context))
    }

    fun setOnline(sharedPreferences: SharedPreferences, uid: String) {
        setOnlineWithDeviceId(uid, getOrCreateDeviceId(sharedPreferences))
    }

    fun setOffline(context: Context, uid: String) {
        setOfflineWithDeviceId(uid, getOrCreateDeviceId(context))
    }

    fun setOffline(sharedPreferences: SharedPreferences, uid: String) {
        setOfflineWithDeviceId(uid, getOrCreateDeviceId(sharedPreferences))
    }

    private fun setOnlineWithDeviceId(uid: String, deviceId: String) {
        if (uid.isBlank() || deviceId.isBlank()) return
        usersCollection.document(uid)
            .update(
                mapOf(
                    "activeSessions.$deviceId" to FieldValue.serverTimestamp(),
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "isOnline" to FieldValue.delete()
                )
            )
            .addOnFailureListener {
                // Abaikan error sementara (mis. sedang offline); heartbeat berikutnya akan menyinkronkan ulang
            }
    }

    private fun setOfflineWithDeviceId(uid: String, deviceId: String) {
        if (uid.isBlank() || deviceId.isBlank()) return
        usersCollection.document(uid)
            .update(
                mapOf(
                    "activeSessions.$deviceId" to FieldValue.delete(),
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "isOnline" to FieldValue.delete()
                )
            )
            .addOnFailureListener {
                // Best-effort offline update; stale timeout 2 menit akan tetap menandai offline jika gagal
            }
    }

    fun cleanupStaleSessions(uid: String, staleDeviceIds: List<String>) {
        if (uid.isBlank() || staleDeviceIds.isEmpty()) return
        val updates = staleDeviceIds.associate { deviceId ->
            "activeSessions.$deviceId" to FieldValue.delete()
        }
        usersCollection.document(uid)
            .update(updates)
            .addOnFailureListener {
                // Best-effort cleanup
            }
    }
}

/**
 * Composable effect yang otomatis mendaftarkan sesi device saat berada di foreground workspace,
 * mengirim heartbeat berkala, dan menghapus sesi device ini saja saat ke background/keluar.
 */
@Composable
fun WorkspacePresenceEffect(userUid: String?) {
    val uid = userUid?.takeIf { it.isNotBlank() } ?: return
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isForeground by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycleOwner, uid) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    isForeground = true
                    WorkspacePresenceManager.setOnline(context, uid)
                }
                Lifecycle.Event.ON_STOP -> {
                    isForeground = false
                    WorkspacePresenceManager.setOffline(context, uid)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            isForeground = true
            WorkspacePresenceManager.setOnline(context, uid)
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            WorkspacePresenceManager.setOffline(context, uid)
        }
    }

    LaunchedEffect(uid, isForeground) {
        if (!isForeground) return@LaunchedEffect
        while (true) {
            delay(ContributorPresenceRules.HEARTBEAT_INTERVAL_MS)
            WorkspacePresenceManager.setOnline(context, uid)
        }
    }
}
