package com.example.careevac.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "staff",
    val institution: String = "",
    val department: String = "",
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false
) {
    constructor() : this("", "", "", "staff", "", "", true, false)

    fun isSuperAdmin() = role == "super_admin"
    fun isAdmin() = role == "admin" || role == "super_admin"
    fun isStaff() = role == "staff" || role == "admin" || role == "super_admin"
}