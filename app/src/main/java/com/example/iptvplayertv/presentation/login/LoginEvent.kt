package com.example.iptvplayertv.presentation.login

sealed class LoginEvent {
    data class HostChanged(val value: String) : LoginEvent()
    data class UsernameChanged(val value: String) : LoginEvent()
    data class PasswordChanged(val value: String) : LoginEvent()
    object Submit : LoginEvent()
}