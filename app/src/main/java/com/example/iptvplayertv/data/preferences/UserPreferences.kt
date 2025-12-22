package com.example.iptvplayertv.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val HOST_KEY = stringPreferencesKey("host")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val EXP_DATE_KEY = stringPreferencesKey("exp_date")
        private val STATUS_KEY = stringPreferencesKey("status")
    }

    val userCredentials: Flow<UserCredentials?> = context.dataStore.data.map { prefs ->
        val host = prefs[HOST_KEY]
        val username = prefs[USERNAME_KEY]
        val password = prefs[PASSWORD_KEY]

        if (host != null && username != null && password != null) {
            UserCredentials(
                host = host,
                username = username,
                password = password,
                expDate = prefs[EXP_DATE_KEY],
                status = prefs[STATUS_KEY]
            )
        } else {
            null
        }
    }

    suspend fun saveCredentials(
        host: String,
        username: String,
        password: String,
        expDate: String?,
        status: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[HOST_KEY] = host
            prefs[USERNAME_KEY] = username
            prefs[PASSWORD_KEY] = password
            if (expDate != null) prefs[EXP_DATE_KEY] = expDate
            if (status != null) prefs[STATUS_KEY] = status
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { it.clear() }
    }
}

data class UserCredentials(
    val host: String,
    val username: String,
    val password: String,
    val expDate: String?,
    val status: String?
)