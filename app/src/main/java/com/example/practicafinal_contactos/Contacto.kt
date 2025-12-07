package com.example.practicafinal_contactos

data class Contacto(
    val codigo: Int,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String,
    val imagen: String,
    val usuario: String
)