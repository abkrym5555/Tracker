package com.example.tracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.AppDatabase
import com.example.tracker.data.SessionPrefs
import com.example.tracker.data.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One-shot events (toast / navigation to the main screen). */
sealed interface AuthEvent {
    data class Message(val text: String) : AuthEvent
    data object LoggedIn : AuthEvent
}

data class AuthUiState(
    val isLogin: Boolean = true,
    val isBusy: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val session = SessionPrefs(app)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun toggleMode() {
        if (_state.value.isBusy) return
        _state.update { it.copy(isLogin = !it.isLogin) }
    }

    fun submit(rawUsername: String, rawPassword: String) {
        val username = rawUsername.trim()
        val password = rawPassword

        if (username.isEmpty() || password.isBlank()) {
            send(AuthEvent.Message("Please fill all fields"))
            return
        }
        if (_state.value.isBusy) return

        val isLogin = _state.value.isLogin
        _state.update { it.copy(isBusy = true) }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { authenticate(isLogin, username, password) }
                when (result) {
                    is AuthResult.Error -> send(AuthEvent.Message(result.message))
                    is AuthResult.Success -> {
                        send(AuthEvent.Message(result.message))
                        send(AuthEvent.LoggedIn)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                send(AuthEvent.Message("Something went wrong. Please try again."))
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    private suspend fun authenticate(isLogin: Boolean, username: String, password: String): AuthResult {
        val dao = db.appDao()
        return if (isLogin) {
            val user = dao.loginUser(username, password)
                ?: return AuthResult.Error("Invalid username or password")
            session.signIn(user.id, user.username)
            AuthResult.Success("Login Successful!")
        } else {
            if (dao.getUserByUsername(username) != null) {
                return AuthResult.Error("Username already exists!")
            }
            val newUserId = dao.registerUser(User(username = username, password = password)).toInt()
            session.signIn(newUserId, username)
            AuthResult.Success("Account Created! Logging in...")
        }
    }

    private fun send(event: AuthEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private sealed interface AuthResult {
        data class Success(val message: String) : AuthResult
        data class Error(val message: String) : AuthResult
    }
}
