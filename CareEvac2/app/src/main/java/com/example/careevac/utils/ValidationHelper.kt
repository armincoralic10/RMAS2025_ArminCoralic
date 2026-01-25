package com.example.careevac.utils

object ValidationHelper {

    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email ne može biti prazan")
            !email.contains("@") -> ValidationResult(false, "Email mora sadržavati @")
            !email.contains(".") -> ValidationResult(false, "Email mora sadržavati domenu (npr. .com)")
            email.indexOf("@") > email.lastIndexOf(".") -> ValidationResult(false, "Neispravan format email-a")
            email.length < 5 -> ValidationResult(false, "Email je prekratak")
            email.startsWith("@") -> ValidationResult(false, "Email ne može počinjati sa @")
            email.endsWith("@") -> ValidationResult(false, "Email ne može završavati sa @")
            email.count { it == '@' } > 1 -> ValidationResult(false, "Email može imati samo jedan @")
            else -> ValidationResult(true, "")
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Lozinka ne može biti prazna")
            password.length < 6 -> ValidationResult(false, "Lozinka mora imati najmanje 6 karaktera")
            password.all { it.isDigit() } -> ValidationResult(false, "Lozinka ne može sadržavati samo brojeve")
            password.all { it.isLetter() } -> ValidationResult(false, "Lozinka mora sadržavati najmanje jedan broj ili simbol")
            password.contains(" ") -> ValidationResult(false, "Lozinka ne može sadržavati razmake")
            else -> ValidationResult(true, "")
        }
    }

    fun validateFullName(fullName: String): ValidationResult {
        return when {
            fullName.isBlank() -> ValidationResult(false, "Ime i prezime ne može biti prazno")
            fullName.length < 3 -> ValidationResult(false, "Ime i prezime je prekratko")
            fullName.trim().split(" ").size < 2 -> ValidationResult(false, "Unesite ime i prezime")
            fullName.any { it.isDigit() } -> ValidationResult(false, "Ime ne može sadržavati brojeve")
            else -> ValidationResult(true, "")
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)