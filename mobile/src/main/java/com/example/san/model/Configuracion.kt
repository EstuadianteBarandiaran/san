package com.example.san.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuraciones")
data class Configuracion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clave: String,
    val valor: String
)