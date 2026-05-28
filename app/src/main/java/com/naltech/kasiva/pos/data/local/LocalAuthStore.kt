package com.naltech.kasiva.pos.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore(name = "local_auth")

class LocalAuthStore(private val context: Context) {
    suspend fun login(emailOrUsername: String, password: String, rememberMe: Boolean): Boolean {
        ensureDefaultAccount()
        val prefs = context.authDataStore.data.first()
        val email = prefs[EMAIL].orEmpty()
        val username = prefs[USERNAME].orEmpty()
        val savedPassword = prefs[PASSWORD].orEmpty()
        val normalizedInput = emailOrUsername.trim()
        val isValidUser = normalizedInput.equals(email, ignoreCase = true) ||
            normalizedInput.equals(username, ignoreCase = true)

        return (isValidUser && password == savedPassword).also { isSuccess ->
            if (isSuccess) {
                context.authDataStore.edit {
                    it[IS_LOGGED_IN] = true
                    it[REMEMBER_ME] = rememberMe
                }
            }
        }
    }

    suspend fun loginDemo(): Boolean = login(DEFAULT_EMAIL, DEFAULT_PASSWORD, rememberMe = true)

    suspend fun ensureDefaultAccount() {
        val prefs = context.authDataStore.data.first()
        if (prefs[EMAIL].isNullOrBlank()) {
            context.authDataStore.edit {
                it[EMAIL] = DEFAULT_EMAIL
                it[USERNAME] = DEFAULT_USERNAME
                it[PASSWORD] = DEFAULT_PASSWORD
                it[IS_LOGGED_IN] = false
                it[REMEMBER_ME] = false
            }
        }
    }

    companion object {
        const val DEFAULT_EMAIL = "kasir@tokosaya.com"
        const val DEFAULT_USERNAME = "kasir"
        const val DEFAULT_PASSWORD = "kasiva123"

        private val EMAIL = stringPreferencesKey("email")
        private val USERNAME = stringPreferencesKey("username")
        private val PASSWORD = stringPreferencesKey("password")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }
}
