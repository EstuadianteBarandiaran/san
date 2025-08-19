package com.example.san.model

data class User(
    val uidUser: String = "",
    val nombre: String = "",
    val edad: Int = 0,
    val estatura: Double = 0.0,
    val peso: Int = 0,
    val cantidadComida: Int = 0,
    val cantidadLitros: Double = 0.0,
    val imc: Double = 0.0
)
