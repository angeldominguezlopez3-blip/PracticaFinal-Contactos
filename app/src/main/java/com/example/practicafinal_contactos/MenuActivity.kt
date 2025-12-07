package com.example.practicafinal_contactos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MenuActivity : AppCompatActivity() {

    private lateinit var cardRegistro: MaterialCardView
    private lateinit var cardVerContactos: MaterialCardView
    private lateinit var btnCerrarSesion: Button
    private lateinit var usuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        usuario = intent.getStringExtra("usuario") ?: ""

        // Inicializar vistas correctamente según el layout
        cardRegistro = findViewById(R.id.cardRegistro)
        cardVerContactos = findViewById(R.id.cardVerContactos)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Configurar click listeners para las cards
        cardRegistro.setOnClickListener {
            val intent = Intent(this, RegistroContactoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        cardVerContactos.setOnClickListener {
            val intent = Intent(this, VerContactosActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            // Limpiar sesión
            getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}