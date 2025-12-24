package com.example.iptvplayertv.presentation.account

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.iptvplayertv.data.preferences.UserPreferences
import com.example.iptvplayertv.data.repository.XtreamRepository
import com.example.iptvplayertv.presentation.home.HomeViewModel.Companion.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val repository: XtreamRepository
) : ViewModel() {

    suspend fun fetchAccountInfo(): AccountUiModel? {
        return try {
            val credentials = userPreferences.userCredentials.firstOrNull()
            if (credentials != null) {
                val accountInfoResult = repository.getAccountInfo(
                    credentials.host,
                    credentials.username,
                    credentials.password
                )

                accountInfoResult.fold(
                    onSuccess = { accountInfo ->
                        Log.d(TAG, "✓ Información de la cuenta: $accountInfo")

                        AccountUiModel(
                            userName = credentials.username,
                            hostUrl = Uri.parse(credentials.host).host ?: "",
                            creationDateFormatted = formatExpDate(accountInfo.userInfo.createdAt),
                            expiryDateFormatted = formatExpDate(accountInfo.userInfo.expDate),
                            isTrial = accountInfo.userInfo.isTrial.equals("1", ignoreCase = true),
                            activeConnections = accountInfo.userInfo.activeCons?.toIntOrNull() ?: 0,
                            maxConnections = accountInfo.userInfo.maxConnections?.toIntOrNull() ?: 0,
                            timeZone = accountInfo.serverInfo.timezone ?: "N/A",
                            status = if (accountInfo.userInfo.status.equals("Active", ignoreCase = true))
                                AccountStatus.ACTIVE else AccountStatus.INACTIVE
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "✗ Error", error)
                        null
                    }
                )
            } else {
                Log.w(TAG, "✗ No hay credenciales guardadas")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error cargando datos", e)
            null
        }
    }

    private fun formatExpDate(timestamp: String?): String {
        if (timestamp == null) return "N/A"

        return try {
            val date = Date(timestamp.toLong() * 1000)
            val format = SimpleDateFormat("MMMM dd, yyyy", Locale("es", "ES"))
            format.format(date)
        } catch (e: Exception) {
            timestamp
        }
    }
}