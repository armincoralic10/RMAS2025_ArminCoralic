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

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

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
                if (repository.isEmailVerified()) {
                    _currentUser.value = user
                    _isLoading.value = false
                    onSuccess()
                } else {
                    repository.logout()
                    _currentUser.value = null
                    _errorMessage.value = "Email nije verifikovan. Provjerite vaš inbox."
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Pogrešan email, lozinka ili je nalog deaktiviran."
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
                try {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    firebaseUser?.sendEmailVerification()

                    repository.logout()
                    _currentUser.value = null
                    _errorMessage.value = "✅ Uspješna registracija! Provjerite email za verifikaciju."
                    _isLoading.value = false
                    onSuccess()
                } catch (e: Exception) {
                    _errorMessage.value = "Registracija uspješna, ali slanje emaila nije uspjelo."
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Greška pri registraciji. Email možda već postoji."
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

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return repository.sendPasswordResetEmail(email)
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val users = repository.getAllUsers()
            _allUsers.value = users
            _isLoading.value = false
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean) {
        val currentList = _allUsers.value.toList()

        _allUsers.value = currentList.map { user ->
            if (user.uid == userId) user.copy(isActive = isActive) else user
        }

        viewModelScope.launch {
            val success = repository.toggleUserStatus(userId, isActive)
            if (!success) {
                _allUsers.value = currentList
                _errorMessage.value = "Greška pri promjeni statusa"
            }
        }
    }

    fun changeUserRole(userId: String, newRole: String) {
        val currentList = _allUsers.value.toList()

        _allUsers.value = currentList.map { user ->
            if (user.uid == userId) user.copy(role = newRole) else user
        }

        viewModelScope.launch {
            val success = repository.changeUserRole(userId, newRole)
            if (!success) {
                _allUsers.value = currentList
                _errorMessage.value = "Greška pri promjeni uloge"
            }
        }
    }

    fun deleteUser(userId: String) {
        val currentList = _allUsers.value.toList()

        _allUsers.value = currentList.filter { it.uid != userId }

        viewModelScope.launch {
            val success = repository.deleteUser(userId)
            if (!success) {
                _allUsers.value = currentList
                _errorMessage.value = "Greška pri brisanju korisnika"
            }
        }
    }
}