package com.example.appcantina.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.appcantina.data.model.AuthState
import com.example.appcantina.data.repository.AuthRepository

class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var authState by mutableStateOf<AuthState?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String) {
        val result = authRepository.login(email, password)
        authState = result.getOrNull()
        message = result.exceptionOrNull()?.message
    }

    fun createAccount(email: String, password: String) {
        val result = authRepository.createAccount(email, password)
        authState = result.getOrNull()
        message = result.exceptionOrNull()?.message ?: "Conta criada."
    }

    fun logout() {
        authState = null
        message = null
    }

    fun clearMessage() {
        message = null
    }
}
