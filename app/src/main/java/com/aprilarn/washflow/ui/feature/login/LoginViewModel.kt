package com.aprilarn.washflow.ui.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.R
import com.aprilarn.washflow.domain.usecase.ManageUserUseCase
import com.aprilarn.washflow.ui.core.utils.UiText
import com.aprilarn.washflow.utils.NetworkStatus
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException

class LoginViewModel(
    private val userUseCase: ManageUserUseCase,
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    private var pendingUserData: UserData? = null
    private var checkJob: Job? = null
    private var lastNetworkStatus: NetworkStatus? = null

    fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }

        result.data?.let {
            checkUserAndWorkspace(it)
        }
    }

    fun checkAutoLogin(userData: UserData) {
        checkUserAndWorkspace(userData)
    }

    fun retryWorkspaceCheck() {
        val userData = pendingUserData ?: return
        if (_state.value.isCheckingWorkspace) return
        checkUserAndWorkspace(userData)
    }

    /**
     * Memicu pengecekan ulang workspace secara otomatis hanya saat jaringan pulih
     * (transisi dari status bermasalah ke [NetworkStatus.Connected]) ketika login sedang tertunda.
     */
    fun onNetworkStatusChanged(status: NetworkStatus) {
        val previous = lastNetworkStatus
        lastNetworkStatus = status
        if (previous != null &&
            previous != NetworkStatus.Connected &&
            status == NetworkStatus.Connected &&
            _state.value.isWaitingForNetwork &&
            !_state.value.isCheckingWorkspace
        ) {
            retryWorkspaceCheck()
        }
    }

    private fun checkUserAndWorkspace(userData: UserData) {
        pendingUserData = userData
        checkJob?.cancel()
        checkJob = scope.launch {
            _state.update {
                it.copy(
                    isCheckingWorkspace = true,
                    showTimeoutDialog = false,
                    isWaitingForNetwork = false,
                    networkErrorMessage = null,
                    signInError = null
                )
            }

            try {
                // Berikan timeout selama 20 detik
                val workspaceId = withTimeout(WORKSPACE_CHECK_TIMEOUT_MS) {
                    userUseCase.saveUserAndGetWorkspace(
                        uid = userData.userId,
                        displayName = userData.displayName,
                        email = userData.email,
                        photoUrl = userData.profilePictureUrl
                    )
                }

                // Update state berdasarkan hasil valid dari server
                _state.update {
                    it.copy(
                        isCheckingWorkspace = false,
                        isWaitingForNetwork = false,
                        networkErrorMessage = null,
                        userHasWorkspace = (workspaceId != null)
                    )
                }
            } catch (_: TimeoutCancellationException) {
                _state.update {
                    it.copy(
                        isCheckingWorkspace = false,
                        showTimeoutDialog = false,
                        isWaitingForNetwork = true,
                        networkErrorMessage = UiText.StringResource(R.string.login_network_slow),
                        userHasWorkspace = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isNetworkOrTransientError(e)) {
                    _state.update {
                        it.copy(
                            isCheckingWorkspace = false,
                            isWaitingForNetwork = true,
                            networkErrorMessage = UiText.StringResource(R.string.login_network_disrupted),
                            userHasWorkspace = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isCheckingWorkspace = false,
                            isWaitingForNetwork = true,
                            networkErrorMessage = UiText.StringResource(R.string.login_workspace_verify_failed),
                            signInError = e.message,
                            userHasWorkspace = null
                        )
                    }
                }
            }
        }
    }

    private fun isNetworkOrTransientError(error: Throwable): Boolean {
        return when (error) {
            is IOException,
            is FirebaseNetworkException -> true
            is FirebaseFirestoreException -> {
                error.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                    error.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ||
                    error.code == FirebaseFirestoreException.Code.ABORTED
            }
            else -> error.cause?.let { isNetworkOrTransientError(it) } == true
        }
    }

    fun resetLoginState() {
        checkJob?.cancel()
        pendingUserData = null
        _state.value = LoginUiState()
    }

    fun onNavigationComplete() {
        pendingUserData = null
        _state.update {
            it.copy(
                userHasWorkspace = null,
                isWaitingForNetwork = false,
                networkErrorMessage = null
            )
        }
    }

    companion object {
        private const val WORKSPACE_CHECK_TIMEOUT_MS = 20_000L
    }
}
