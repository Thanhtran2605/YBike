package com.example.doan.util


sealed class RegisterValidation(){
    object Success: RegisterValidation()

    /* Using class because if failure it's send message. : This is extend */
    data class Failed(val message: String): RegisterValidation()
}

data class RegisterFieldsState(
    val email: RegisterValidation,
    val password: RegisterValidation
)
