package com.example.tourapp.dataModel

data class Login(var email :String, var password:String) {

    override fun toString(): String {
        return "Correo: $email y contraseña: $password"
    }
}