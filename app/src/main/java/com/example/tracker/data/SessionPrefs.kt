package com.example.tracker.data

import android.content.Context

class SessionPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.getBoolean(KEY_LOGGED_IN, false)

    val username: String get() = prefs.getString(KEY_USER, null)?.trim().orEmpty()

    val userId: Int get() = prefs.getInt(KEY_USER_ID, 0)

    fun signIn(userId: Int, username: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER, username)
            .putInt(KEY_USER_ID, userId)
            .apply()
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun budgetFor(username: String = this.username): Double =
        prefs.getString(budgetKey(username), null)?.toDoubleOrNull() ?: 0.0

    fun saveBudget(value: Double) {
        prefs.edit().putString(budgetKey(username), value.toString()).apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_LOGGED_IN)
            .remove(KEY_USER)
            .remove(KEY_USER_ID)
            .apply()
    }

    private fun budgetKey(username: String) = "budget_$username"

    private companion object {
        const val NAME = "AuthPrefs"
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_USER = "logged_user"
        const val KEY_USER_ID = "logged_user_id"
    }
}
