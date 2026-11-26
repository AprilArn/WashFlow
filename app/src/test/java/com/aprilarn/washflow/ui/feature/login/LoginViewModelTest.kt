package com.aprilarn.washflow.ui.feature.login

import com.aprilarn.washflow.domain.repository.UserRepositoryContract
import com.aprilarn.washflow.domain.usecase.ManageUserUseCase
import com.aprilarn.washflow.utils.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {

    private class FakeUserRepository(
        private var handler: suspend () -> String?
    ) : UserRepositoryContract {
        var callCount: Int = 0
            private set

        fun setHandler(newHandler: suspend () -> String?) {
            handler = newHandler
        }

        override suspend fun saveUserAndGetWorkspace(
            uid: String,
            displayName: String?,
            email: String?,
            photoUrl: String?
        ): String? {
            callCount++
            return handler()
        }
    }

    private val sampleUser = UserData(
        userId = "uid-1",
        displayName = "Test User",
        email = "test@example.com",
        profilePictureUrl = null
    )

    private fun createUnconfinedScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    @Test
    fun networkError_keepsUserHasWorkspaceNull_andSetsWaitingForNetwork() = runBlocking {
        val repo = FakeUserRepository { throw IOException("No internet") }
        val viewModel = LoginViewModel(ManageUserUseCase(repo), createUnconfinedScope())

        viewModel.checkAutoLogin(sampleUser)

        val state = viewModel.state.value
        assertNull("Error jaringan tidak boleh dianggap userHasWorkspace = false", state.userHasWorkspace)
        assertTrue(state.isWaitingForNetwork)
        assertFalse(state.isCheckingWorkspace)
        assertEquals(1, repo.callCount)
    }

    @Test
    fun autoRetry_runsOnOfflineToConnectedTransition_andEntersMainWhenWorkspaceExists() = runBlocking {
        val repo = FakeUserRepository { throw IOException("Offline") }
        val viewModel = LoginViewModel(ManageUserUseCase(repo), createUnconfinedScope())

        viewModel.onNetworkStatusChanged(NetworkStatus.Offline)
        viewModel.checkAutoLogin(sampleUser)
        assertNull(viewModel.state.value.userHasWorkspace)
        assertTrue(viewModel.state.value.isWaitingForNetwork)

        // Simulasikan koneksi kembali normal dan workspace valid ditemukan
        repo.setHandler { "workspace-123" }
        viewModel.onNetworkStatusChanged(NetworkStatus.Connected)

        val recovered = viewModel.state.value
        assertEquals(true, recovered.userHasWorkspace)
        assertFalse(recovered.isWaitingForNetwork)
        assertEquals(2, repo.callCount)
    }

    @Test
    fun autoRetry_runsOnPoorConnectionToConnectedTransition() = runBlocking {
        val repo = FakeUserRepository { throw IOException("Slow timeout") }
        val viewModel = LoginViewModel(ManageUserUseCase(repo), createUnconfinedScope())

        viewModel.onNetworkStatusChanged(NetworkStatus.PoorConnection)
        viewModel.checkAutoLogin(sampleUser)
        assertTrue(viewModel.state.value.isWaitingForNetwork)

        repo.setHandler { "workspace-abc" }
        viewModel.onNetworkStatusChanged(NetworkStatus.Connected)

        assertEquals(true, viewModel.state.value.userHasWorkspace)
        assertFalse(viewModel.state.value.isWaitingForNetwork)
        assertEquals(2, repo.callCount)
    }

    @Test
    fun noRetryLoop_whenNetworkStatusWasAlreadyConnected_andManualRetryWorks() = runBlocking {
        val repo = FakeUserRepository { throw IOException("Transient error") }
        val viewModel = LoginViewModel(ManageUserUseCase(repo), createUnconfinedScope())

        viewModel.onNetworkStatusChanged(NetworkStatus.Connected)
        viewModel.checkAutoLogin(sampleUser)
        assertEquals(1, repo.callCount)
        assertTrue(viewModel.state.value.isWaitingForNetwork)

        // Emisi Connected berulang tidak memicu retry loop
        viewModel.onNetworkStatusChanged(NetworkStatus.Connected)
        assertEquals(1, repo.callCount)

        // Retry manual dari tombol "Coba Lagi" berhasil
        repo.setHandler { "workspace-999" }
        viewModel.retryWorkspaceCheck()
        assertEquals(2, repo.callCount)
        assertEquals(true, viewModel.state.value.userHasWorkspace)
        assertFalse(viewModel.state.value.isWaitingForNetwork)
    }

    @Test
    fun genuineNullWorkspace_setsUserHasWorkspaceFalse() = runBlocking {
        val repo = FakeUserRepository { null }
        val viewModel = LoginViewModel(ManageUserUseCase(repo), createUnconfinedScope())

        viewModel.checkAutoLogin(sampleUser)

        val state = viewModel.state.value
        assertEquals(false, state.userHasWorkspace)
        assertFalse(state.isWaitingForNetwork)
        assertFalse(state.isCheckingWorkspace)
    }

    @Test
    fun genuineWorkspaceId_setsUserHasWorkspaceTrue() = runBlocking {
        val repo = FakeUserRepository { "ws-valid" }
        val viewModel = LoginViewModel(ManageUserUseCase(repo), createUnconfinedScope())

        viewModel.checkAutoLogin(sampleUser)

        val state = viewModel.state.value
        assertEquals(true, state.userHasWorkspace)
        assertFalse(state.isWaitingForNetwork)
        assertFalse(state.isCheckingWorkspace)
    }
}
