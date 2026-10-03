package com.example.tagihsumberdanti.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tagih_sumberdanti_pref", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    fun saveSession(username: String, role: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun clearSession() {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.putString(KEY_USERNAME, "")
        editor.putString(KEY_ROLE, "")
        editor.apply()
    }
}
