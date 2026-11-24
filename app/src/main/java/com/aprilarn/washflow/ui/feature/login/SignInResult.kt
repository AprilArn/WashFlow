package com.aprilarn.washflow.ui.feature.login

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val displayName: String?,
    val profilePictureUrl: String?,
    val email: String?
)
