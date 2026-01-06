package com.example.careevac.model

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "staff",
    val institution: String = "",
    val department: String = "",
    val isActive: Boolean = true
) {
    constructor() : this("", "", "", "staff", "", "", true)

    fun isSuperAdmin() = role == "super_admin"
    fun isAdmin() = role == "admin" || role == "super_admin"
    fun isStaff() = role == "staff" || role == "admin" || role == "super_admin"
}