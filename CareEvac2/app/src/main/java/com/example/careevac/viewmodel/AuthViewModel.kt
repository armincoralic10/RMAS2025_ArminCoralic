package com.example.careevac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careevac.data.repository.AuthRepository
import com.example.careevac.model.User
import com.example.careevac.utils.ValidationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        val emailRes = ValidationHelper.validateEmail(email)
        val passRes = ValidationHelper.validatePassword(password)

        if (!emailRes.isValid) { _errorMessage.value = emailRes.errorMessage; return }
        if (!passRes.isValid) { _errorMessage.value = passRes.errorMessage; return }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val user = repository.login(email, password)

            if (user != null) {
                if (repository.isEmailVerified()) {

                    FirebaseFirestore.getInstance().collection("users")
                        .document(user.uid)
                        .update("isEmailVerified", true)

                    _currentUser.value = user.copy(isEmailVerified = true)
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

    fun register(email: String, password: String, fullName: String, onSuccess: () -> Unit) {
        val nameRes = ValidationHelper.validateFullName(fullName)
        val emailRes = ValidationHelper.validateEmail(email)
        val passRes = ValidationHelper.validatePassword(password)

        if (!nameRes.isValid) { _errorMessage.value = nameRes.errorMessage; return }
        if (!emailRes.isValid) { _errorMessage.value = emailRes.errorMessage; return }
        if (!passRes.isValid) { _errorMessage.value = passRes.errorMessage; return }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val userId = repository.register(email, password, fullName)

            if (userId != null) {
                val firebaseUser = FirebaseAuth.getInstance().currentUser

                if (firebaseUser != null) {
                    firebaseUser.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                repository.logout()
                                _currentUser.value = null
                                _errorMessage.value = "Uspješna registracija! Verifikujte email prije prijave."
                                _isLoading.value = false
                                onSuccess()
                            } else {
                                _errorMessage.value = "Registracija uspješna, ali slanje emaila nije uspjelo."
                                _isLoading.value = false
                            }
                        }
                } else {
                    _errorMessage.value = "Greška: Korisnik nije prepoznat nakon registracije."
                    _isLoading.value = false
                }
            } else {
                _errorMessage.value = "Greška pri registraciji (moguće da email već postoji)."
                _isLoading.value = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        repository.logout()
        _currentUser.value = null
        onSuccess()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _allUsers.value = repository.getAllUsers()
            _isLoading.value = false
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            val success = repository.toggleUserStatus(userId, isActive)
            if (success) loadAllUsers()
        }
    }

    fun changeUserRole(userId: String, newRole: String) {
        viewModelScope.launch {
            val success = repository.changeUserRole(userId, newRole)
            if (success) loadAllUsers()
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val success = repository.deleteUser(userId)
            if (success) loadAllUsers()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean = repository.sendPasswordResetEmail(email)
}