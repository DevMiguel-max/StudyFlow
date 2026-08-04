package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.domain.repository.AuthRepository
import com.studyflow.app.domain.repository.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    val currentUser: StateFlow<AuthUser?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.signInWithEmail(email, pass)
            if (result.isSuccess) {
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.signUpWithEmail(email, pass)
            if (result.isSuccess) {
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }

    fun recoverPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.recoverPassword(email)
            if (result.isSuccess) {
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            repository.deleteAccount()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
