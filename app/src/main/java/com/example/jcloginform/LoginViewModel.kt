package com.example.jcloginform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Patterns

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isLoginEnabled: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object LoginClicked : LoginEvent()
}

class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginState())
        private set

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                state = state.copy(
                    email = event.email,
                    isEmailError = false,
                    isLoginEnabled = isValidInput(event.email, state.password)
                )
            }
            is LoginEvent.PasswordChanged -> {
                state = state.copy(
                    password = event.password,
                    isPasswordError = false,
                    isLoginEnabled = isValidInput(state.email, event.password)
                )
            }
            LoginEvent.LoginClicked -> {
                val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
                val isPasswordValid = state.password.length >= 6

                state = state.copy(
                    isEmailError = !isEmailValid,
                    isPasswordError = !isPasswordValid
                )
            }
        }
    }

    private fun isValidInput(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }
} 