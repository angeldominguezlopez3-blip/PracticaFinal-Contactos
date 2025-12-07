package com.example.practicafinal_contactos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var btnRegistroContactos: Button
    private lateinit var btnVerContactos: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var usuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        usuario = intent.getStringExtra("usuario") ?: ""

        btnRegistroContactos = findViewById(R.id.btnRegistroContactos)
        btnVerContactos = findViewById(R.id.btnVerContactos)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnRegistroContactos.setOnClickListener {
            val intent = Intent(this, RegistroContactoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        btnVerContactos.setOnClickListener {
            val intent = Intent(this, VerContactosActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            // Limpiar sesión
            getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply()
            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}