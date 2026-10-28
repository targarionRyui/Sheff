package com.example.sheff.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.sheff.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object SessionManager {

    private const val PREFS_NAME = "sheff_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_LOGIN = "user_login"

    private var preferences: SharedPreferences? = null

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()


    fun init(context: Context) {
        val storage = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences = storage

        val savedId = storage.getString(KEY_USER_ID, null)
        val savedLogin = storage.getString(KEY_USER_LOGIN, null)
        if (savedId != null && savedLogin != null) {

            _currentUser.value = User(id = savedId, login = savedLogin, password = "")
        }
    }

    fun signIn(user: User) {
        _currentUser.value = user
        preferences?.edit()
            ?.putString(KEY_USER_ID, user.id)
            ?.putString(KEY_USER_LOGIN, user.login)
            ?.apply()
    }

    fun signOut() {
        _currentUser.value = null
        preferences?.edit()?.clear()?.apply()
    }
}
