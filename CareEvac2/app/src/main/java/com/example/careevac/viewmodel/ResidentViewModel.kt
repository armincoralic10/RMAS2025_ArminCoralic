package com.example.careevac.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.careevac.data.repository.ResidentRepository
import com.example.careevac.model.Resident
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ResidentViewModel(
    private val repository: ResidentRepository = ResidentRepository()
) : ViewModel() {

    private val _residents = MutableStateFlow<List<Resident>>(emptyList())
    val residents: StateFlow<List<Resident>> = _residents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastOptimisticUpdateTime = 0L

    val evacuatedCount: StateFlow<Int> = residents.map { list ->
        list.count { it.isEvacuated }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val waitingCount: StateFlow<Int> = residents.map { list ->
        list.count { !it.isEvacuated }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadResidents()
    }

    private fun loadResidents() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getAllResidents()
                .catch { e ->
                    _errorMessage.value = "Greška: ${e.message}"
                    _isLoading.value = false
                }
                .collect { list ->
                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastUpdate = currentTime - lastOptimisticUpdateTime

                    if (timeSinceLastUpdate < 500) {
                        return@collect
                    }

                    _residents.value = list.sortedByPriority()
                    _isLoading.value = false
                }
        }
    }

    fun markAsEvacuated(residentId: String) {
        viewModelScope.launch {
            val residentBefore = _residents.value.find { it.id == residentId }
            if (residentBefore == null) return@launch

            val updatedList = _residents.value.map { resident ->
                if (resident.id == residentId) {
                    resident.copy(isEvacuated = true)
                } else {
                    resident
                }
            }.sortedByPriority()

            lastOptimisticUpdateTime = System.currentTimeMillis()
            _residents.value = updatedList

            val success = repository.markAsEvacuated(residentId)

            if (success) {
                delay(600)
                lastOptimisticUpdateTime = 0L
            } else {
                _errorMessage.value = "Greška pri evakuaciji stanara"
                lastOptimisticUpdateTime = 0L

                _residents.value = _residents.value.map { resident ->
                    if (resident.id == residentId) {
                        resident.copy(isEvacuated = false)
                    } else {
                        resident
                    }
                }.sortedByPriority()
            }
        }
    }

    fun resetAllEvacuations() {
        viewModelScope.launch {
            val updatedList = _residents.value.map { resident ->
                resident.copy(isEvacuated = false)
            }.sortedByPriority()

            lastOptimisticUpdateTime = System.currentTimeMillis()
            _residents.value = updatedList

            val success = repository.resetAllEvacuations()

            if (success) {
                delay(600)
                lastOptimisticUpdateTime = 0L
            } else {
                _errorMessage.value = "Greška pri resetu evakuacija"
                lastOptimisticUpdateTime = 0L
            }
        }
    }

    private fun List<Resident>.sortedByPriority() = sortedWith(
        compareBy<Resident> { it.isEvacuated }
            .thenBy {
                when (it.mobilityStatus.uppercase()) {
                    "NEPOKRETAN" -> 0
                    "OTEZANO POKRETAN", "OTEŽANO POKRETAN" -> 1
                    else -> 2
                }
            }
            .thenBy { it.fullName }
    )

    fun clearError() {
        _errorMessage.value = null
    }
}