package com.example.iptvplayertv.presentation.login

data class LoginState(
    val host: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)