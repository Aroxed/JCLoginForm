package com.example.jcloginform

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jcloginform.data.LoginApi
import com.example.jcloginform.data.LoginRequest
import com.example.jcloginform.data.LogoutRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class Screen {
    object Login : Screen()
    object Profile : Screen()
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isLoginEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentScreen: Screen = Screen.Login,
    val isAuthenticated: Boolean = false
)

// sealed - only this file
sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent() // singleton (data)
    data class PasswordChanged(val password: String) : LoginEvent() // singleton (data)
    object LoginClicked : LoginEvent() // singleton (no need data)
    object LogoutClicked : LoginEvent() // singleton (no need data)
}

class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginState())
        private set

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/") // Android emulator localhost
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val loginApi = retrofit.create(LoginApi::class.java)

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                // state.copy is important! = reactivity!
                state = state.copy(
                    email = event.email,
                    isEmailError = false,
                    errorMessage = null,
                    isLoginEnabled = isValidInput(event.email, state.password)
                )
            }
            is LoginEvent.PasswordChanged -> {
                state = state.copy(
                    password = event.password,
                    isPasswordError = false,
                    errorMessage = null,
                    isLoginEnabled = isValidInput(state.email, event.password)
                )
            }
            LoginEvent.LoginClicked -> {
                val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
                val isPasswordValid = state.password.length >= 6

                if (!isEmailValid || !isPasswordValid) {
                    state = state.copy(
                        isEmailError = !isEmailValid,
                        isPasswordError = !isPasswordValid
                    )
                    return
                }

                login()
            }
            LoginEvent.LogoutClicked -> {
                logout()
            }
        }
    }

    private fun login() {
        // viewModelScope is a coroutine scope (handles lifecycle of ViewModel automatically)
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                val response = loginApi.login(LoginRequest(state.email, state.password))
                if (response.isSuccessful) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = null,
                        currentScreen = Screen.Profile,
                        isAuthenticated = true
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = response.message()
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                val response = loginApi.logout(LogoutRequest(state.email))
                if (response.isSuccessful) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = null,
                        currentScreen = Screen.Login,
                        isAuthenticated = false,
                        email = "",
                        password = ""
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = response.message()
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }

    private fun isValidInput(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }
} 