package com.example.iptvplayertv.presentation.account

import com.google.gson.annotations.SerializedName

enum class AccountStatus {
    ACTIVE,
    EXPIRED,
    WARNING,
    INACTIVE
}

data class AccountUiModel(
    val userName: String = "",
    val hostUrl: String = "",
    val creationDateFormatted: String = "",
    val expiryDateFormatted: String = "",
    val isTrial: Boolean = false,
    val activeConnections: Int = 0,
    val maxConnections: Int = 0,
    val timeZone: String = "",
    val status: AccountStatus = AccountStatus.ACTIVE
)

interface AccountUiState{
    object Loading
    data class Success(val data: AccountUiModel)
    data class Error(val message: String)
}
