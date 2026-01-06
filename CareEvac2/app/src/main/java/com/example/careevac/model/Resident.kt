package com.example.careevac.model

data class Resident(
    val id: String = "",
    val fullName: String = "",
    val roomNumber: String = "",
    val mobilityStatus: String = "",
    val emergencyContact: String = "",
    val medicalNotes: String = "",
    val isEvacuated: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", false)
}