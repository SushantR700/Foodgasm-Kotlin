package com.example.foodgasm.utils

sealed class RegisterValidation{
    object Success: RegisterValidation()
    data class Failed(var message:String):RegisterValidation()

}

data class RegisterFieldState(
    val email:RegisterValidation,
    val password:RegisterValidation
)
