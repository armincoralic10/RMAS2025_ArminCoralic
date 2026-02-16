package com.example.careevac.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "staff",
    val institution: String = "",
    val department: String = "",

    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,

    @get:PropertyName("isEmailVerified")
    @set:PropertyName("isEmailVerified")
    var isEmailVerified: Boolean = false
) {
    constructor() : this("", "", "", "staff", "", "", true, false)

    @Exclude
    fun isSuperAdmin() = role == "super_admin"

    @Exclude
    fun isAdmin() = role == "admin" || role == "super_admin"

    @Exclude
    fun isStaff() = role == "staff" || role == "admin" || role == "super_admin"
}