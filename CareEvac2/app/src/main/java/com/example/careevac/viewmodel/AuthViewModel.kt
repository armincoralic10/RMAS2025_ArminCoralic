package com.example.careevac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careevac.data.repository.AuthRepository
import com.example.careevac.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            if (repository.isUserLoggedIn()) {
                _currentUser.value = repository.getCurrentUser()
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val user = repository.login(email, password)

            if (user != null) {
                _currentUser.value = user
                _isLoading.value = false
                onSuccess()
            } else {
                _errorMessage.value = "Pogrešan email ili lozinka"
                _isLoading.value = false
            }
        }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val userId = repository.register(email, password, fullName)

            if (userId != null) {
                login(email, password, onSuccess)
            } else {
                _errorMessage.value = "Greška pri registraciji. Provjerite da li email već postoji."
                _isLoading.value = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        repository.logout()
        _currentUser.value = null
        onSuccess()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}