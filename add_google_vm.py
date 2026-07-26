import re

with open("app/src/main/java/com/example/presentation/viewmodel/AuthViewModel.kt", "r") as f:
    content = f.read()

google_signin = """
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = repository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = result.exceptionOrNull()?.message ?: "Erro ao entrar com Google")
            }
        }
    }
"""

content = content.replace("    fun recoverPassword", google_signin + "\n    fun recoverPassword")

with open("app/src/main/java/com/example/presentation/viewmodel/AuthViewModel.kt", "w") as f:
    f.write(content)
