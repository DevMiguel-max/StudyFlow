package com.studyflow.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
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
        val message = e?.message.orEmpty()
        return when (e) {
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos"
            is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado"
            is FirebaseAuthWeakPasswordException -> "Senha muito fraca"
            is FirebaseNetworkException -> "Sem conexão"
            is FirebaseAuthException -> when {
                e.errorCode == "ERROR_OPERATION_NOT_ALLOWED" ||
                e.errorCode == "OPERATION_NOT_ALLOWED" ||
                message.contains("operation is not allowed", ignoreCase = true) ->
                    "Cadastro por e-mail não está habilitado no Firebase"
                e.errorCode in listOf("ERROR_INVALID_EMAIL", "INVALID_EMAIL") -> "E-mail inválido"
                e.errorCode in listOf("ERROR_TOO_MANY_REQUESTS", "TOO_MANY_REQUESTS") -> "Muitas tentativas. Tente mais tarde"
                e.errorCode in listOf("ERROR_EMAIL_ALREADY_IN_USE", "EMAIL_ALREADY_IN_USE") -> "Este e-mail já está cadastrado"
                else -> "Ocorreu um erro (${e.javaClass.simpleName}). Tente novamente."
            }
            is FirebaseException -> if (
                message.contains("App Check", ignoreCase = true) ||
                message.contains("attestation", ignoreCase = true) ||
                message.contains("blocked", ignoreCase = true) ||
                message.contains("internal error", ignoreCase = true)
            ) {
                "Falha de verificação do app (App Check). Tente novamente ou contate o suporte"
            } else {
                "Ocorreu um erro (${e.javaClass.simpleName}). Tente novamente."
            }
            else -> "Ocorreu um erro (${e?.javaClass?.simpleName}). Tente novamente."
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
