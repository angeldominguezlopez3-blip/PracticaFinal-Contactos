package com.example.practicafinal_contactos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegistrarse: TextView
    private val url = "http://192.168.1.100/gestioncontactos/" // Cambia por tu IP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUsuario(usuario, password)
        }

        tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun loginUsuario(usuario: String, password: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("usuario", usuario)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${url}login.php",
            jsonObject,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    // Guardar usuario en SharedPreferences
                    val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("usuario", usuario)
                        apply()
                    }

                    // Ir al menú principal
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("usuario", usuario)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}