package com.studyflow.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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

    private fun mapAuthException(e: Throwable?): String {
        if (e != null) {
            Log.e("AuthViewModel", "Erro de autenticação", e)
        }
        return when (e) {
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos"
            is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado"
            is FirebaseAuthWeakPasswordException -> "Senha muito fraca"
            is FirebaseNetworkException -> "Sem conexão"
            else -> "Ocorreu um erro. Tente novamente."
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.signInWithEmail(email, pass)
            if (result.isSuccess) {
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = mapAuthException(result.exceptionOrNull()))
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
                _state.value = AuthState(error = mapAuthException(result.exceptionOrNull()))
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
                _state.value = AuthState(error = mapAuthException(result.exceptionOrNull()))
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
                _state.value = AuthState(error = mapAuthException(result.exceptionOrNull()))
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
