package com.example.practicafinal_contactos

data class Contacto(
    val codigo: Int,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String,
    val imagen: String, // Ahora almacena la URL completa de ImgBB
    val usuario: String
)