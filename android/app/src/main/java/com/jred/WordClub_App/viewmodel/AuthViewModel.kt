package com.jred.WordClub_App.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jred.WordClub_App.data.api.RetrofitClient
import com.jred.WordClub_App.data.model.UserInfo
import com.jred.WordClub_App.data.repository.AuthRepository
import com.jred.WordClub_App.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: UserInfo? = null,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository = AuthRepository(tokenManager)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        RetrofitClient.init(tokenManager)
        // 检查是否已有 token
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.firstOrNull()
            if (!token.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.fetchUser().fold(
                    onSuccess = { user ->
                        _uiState.value = AuthUiState(isLoggedIn = true, user = user)
                    },
                    onFailure = {
                        _uiState.value = AuthUiState(isLoggedIn = false)
                    }
                )
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请填写用户名和密码")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.login(username, password).fold(
                onSuccess = { auth ->
                    _uiState.value = AuthUiState(isLoggedIn = true, user = auth.user)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请填写所有字段")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "密码最少 6 位")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.register(username, email, password).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState(isLoggedIn = false)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
