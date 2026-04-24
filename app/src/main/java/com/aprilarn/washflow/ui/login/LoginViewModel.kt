// com/aprilarn/washflow/ui/login/LoginViewModel.kt

package com.aprilarn.washflow.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.UserRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()
    private val userRepository = UserRepository()

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(isSignInSuccessful = result.data != null, signInError = result.errorMessage) }

        result.data?.let {
            checkUserAndWorkspace(it)
        }
    }

    fun checkAutoLogin(userData: UserData) {
        checkUserAndWorkspace(userData)
    }

    private fun checkUserAndWorkspace(userData: UserData) {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingWorkspace = true, showTimeoutDialog = false) }

            try {
                // Berikan timeout selama 10 detik
                val workspaceId = withTimeout(10000L) {
                    // kotlinx.coroutines.delay(11000L)
                    userRepository.saveUserAndGetWorkspace(
                        uid = userData.userId,
                        displayName = userData.displayName,
                        email = userData.email,
                        photoUrl = userData.profilePictureUrl
                    )
                }

                // Update state berdasarkan hasil dari fungsi repositori
                _state.update {
                    it.copy(
                        isCheckingWorkspace = false,
                        userHasWorkspace = (workspaceId != null) // true jika workspaceId tidak null
                    )
                }
            } catch (e: TimeoutCancellationException) {
                // Jika sudah 10 detik dan masih loading
                _state.update {
                    it.copy(
                        isCheckingWorkspace = false,
                        showTimeoutDialog = true
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isCheckingWorkspace = false,
                        signInError = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    fun resetLoginState() {
        _state.value = LoginUiState()
    }

    fun onNavigationComplete() {
        _state.update { it.copy(userHasWorkspace = null) }
    }
}