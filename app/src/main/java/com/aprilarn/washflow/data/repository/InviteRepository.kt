package com.aprilarn.washflow.data.repository

import com.aprilarn.washflow.data.model.Invites
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.random.Random

class InviteRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val workspacesCollection = db.collection("workspaces")
    private val invitesCollection = db.collection("invites")

    private suspend fun getCurrentWorkspaceId(): String? {
        val user = Firebase.auth.currentUser ?: return null
        return try {
            usersCollection.document(user.uid).get().await().getString("workspaceId")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createInvite(maxContributors: Int, expiresAt: Timestamp): Invites? {
        val workspaceId = getCurrentWorkspaceId() ?: return null
        return try {
            val inviteCode = generateSequence {
                (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
            }.first { code ->
                runBlocking { invitesCollection.document(code).get().await().exists().not() }
            }

            val newInvite = Invites(
                inviteId = inviteCode,
                workspaceId = workspaceId,
                maxContributors = maxContributors,
                expiresAt = expiresAt,
                status = "active",
                createdAt = Timestamp.now()
            )
            invitesCollection.document(inviteCode).set(newInvite).await()
            newInvite
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * FUNGSI YANG DIPERBARUI:
     * Mendengarkan dokumen pengguna, lalu mencari invite aktif
     * berdasarkan workspaceId yang didapat.
     */
    fun getActiveInviteForCurrentWorkspace(): Flow<Invites?> {
        return callbackFlow {
            val user = Firebase.auth.currentUser
            if (user == null) {
                trySend(null)
                close()
                return@callbackFlow
            }

            var inviteListener: ListenerRegistration? = null
            val userDocRef = usersCollection.document(user.uid)

            // 1. UTAMA: Dengarkan dokumen pengguna
            val userListener = userDocRef.addSnapshotListener { userSnapshot, userError ->
                if (userError != null) {
                    close(userError)
                    return@addSnapshotListener
                }

                // Hapus listener invite yang lama
                inviteListener?.remove()

                val workspaceId = userSnapshot?.getString("workspaceId")

                if (workspaceId.isNullOrEmpty()) {
                    // Tidak ada workspace, tidak ada invite
                    trySend(null)
                } else {
                    // Ada workspace, cari invite aktif
                    val query = invitesCollection
                        .whereEqualTo("workspaceId", workspaceId)
                        .whereEqualTo("status", "active")
                        .limit(1)

                    inviteListener = query.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null) // Kirim null jika ada error
                            return@addSnapshotListener
                        }
                        val activeInvite = snapshot?.documents?.firstOrNull()?.toObject(Invites::class.java)
                        trySend(activeInvite)
                    }
                }
            }

            // Saat flow ditutup, hapus kedua listener
            awaitClose {
                userListener.remove()
                inviteListener?.remove()
            }
        }
    }

    suspend fun expireInvite(inviteId: String): Boolean {
        return try {
            invitesCollection.document(inviteId).update("status", "expired").await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun joinWorkspace(inviteCode: String): Pair<Boolean, String> {
        val user = Firebase.auth.currentUser ?: return Pair(false, "User not logged in.")
        val inviteDocRef = invitesCollection.document(inviteCode)

        try {
            // --- BAGIAN 1: BACA & VALIDASI (DI LUAR TRANSAKSI) ---
            // Kita baca data undangan 1x di luar transaksi utama
            val inviteSnapshot = inviteDocRef.get().await()

            if (!inviteSnapshot.exists()) {
                return Pair(false, "Kode tidak valid")
            }

            val inviteData = inviteSnapshot.toObject(Invites::class.java)
                ?: return Pair(false, "Gagal memuat data undangan.")

            // 1. Validasi Tanggal Kedaluwarsa (Prioritas 1)
            val expiresAt = inviteData.expiresAt
            if (expiresAt != null && expiresAt.toDate().before(Date())) {
                // INI PERBAIKANNYA: Jika kedaluwarsa, update status di luar transaksi
                if (inviteData.status != "expired") {
                    inviteDocRef.update("status", "expired").await() // Update terpisah
                }
                // Kembalikan error
                return Pair(false, "Kode undangan sudah kedaluwarsa")
            }

            // 2. Validasi Status (Prioritas 2)
            if (inviteData.status != "active") {
                if (inviteData.status == "used") return Pair(false, "Kode undangan sudah penuh")
                // Menangkap status 'expired' manual atau status tidak valid lainnya
                return Pair(false, "Kode undangan sudah tidak berlaku")
            }

            // 3. Validasi Kuota (Prioritas 3)
            val maxContributors = inviteData.maxContributors ?: 0
            if (inviteData.usersWhoJoined.size >= maxContributors) {
                // Kuota habis, update status di luar transaksi
                if (inviteData.status != "used") {
                    inviteDocRef.update("status", "used").await() // Update terpisah
                }
                // Kembalikan error
                return Pair(false, "Kode undangan sudah penuh")
            }

            // --- BAGIAN 2: EKSEKUSI JOIN (DI DALAM TRANSAKSI) ---
            // Jika semua validasi lolos, baru kita jalankan transaksi untuk bergabung.

            var finalWorkspaceId: String? = null
            db.runTransaction { transaction ->
                // Kita tidak perlu 'get' lagi karena sudah punya 'inviteData'

                val workspaceId = inviteData.workspaceId
                    ?: throw Exception("Workspace ID tidak ditemukan dalam undangan.")
                finalWorkspaceId = workspaceId

                val workspaceDocRef = workspacesCollection.document(workspaceId)
                val userDocRef = usersCollection.document(user.uid)

                val newUsersWhoJoined = inviteData.usersWhoJoined + user.uid

                // Update 3 dokumen secara atomik
                transaction.update(inviteDocRef, "usersWhoJoined", newUsersWhoJoined)
                transaction.update(workspaceDocRef, "contributors.${user.uid}", "member")
                transaction.update(userDocRef, "workspaceId", workspaceId)

                // Jika kuota TEPAT TERPENUHI setelah user ini join, update status
                if (newUsersWhoJoined.size >= maxContributors) {
                    transaction.update(inviteDocRef, "status", "used")
                }

            }.await()
            return Pair(true, finalWorkspaceId!!)

        } catch (e: Exception) {
            // Catch block ini sekarang hanya menangani kegagalan pada Transaksi (Bagian 2)
            return Pair(false, e.message ?: "Gagal bergabung ke workspace.")
        }
    }
}