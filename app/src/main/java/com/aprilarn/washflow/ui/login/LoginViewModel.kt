// com/aprilarn/washflow/ui/login/LoginViewModel.kt

package com.aprilarn.washflow.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    private fun checkUserAndWorkspace(userData: UserData) {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingWorkspace = true) }

            // Panggil fungsi baru yang lebih aman
            val workspaceId = userRepository.saveUserAndGetWorkspace(
                uid = userData.userId,
                displayName = userData.displayName,
                email = userData.email,
                photoUrl = userData.profilePictureUrl
            )

            // Update state berdasarkan hasil dari fungsi repositori
            _state.update {
                it.copy(
                    isCheckingWorkspace = false,
                    userHasWorkspace = (workspaceId != null) // true jika workspaceId tidak null
                )
            }
        }
    }

    fun onNavigationComplete() {
        _state.update { it.copy(userHasWorkspace = null) }
    }
}