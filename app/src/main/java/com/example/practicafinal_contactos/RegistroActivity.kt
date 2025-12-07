package com.example.practicafinal_contactos

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class RegistroActivity : AppCompatActivity() {

    private lateinit var etUsuario: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var btnVolverLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar vistas
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)

        // Configurar listener del botón registrar
        btnRegistrar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrarUsuario(usuario, password)
        }

        // Configurar listener del botón volver
        btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun registrarUsuario(usuario: String, password: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("usuario", usuario)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}registrar_usuario.php",
            jsonObject,
            { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(
                        this,
                        "Usuario registrado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        response.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error: ${error.message ?: "Desconocido"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        queue.add(request)
    }
}