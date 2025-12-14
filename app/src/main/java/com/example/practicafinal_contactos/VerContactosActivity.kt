package com.example.practicafinal_contactos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject

class VerContactosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactoAdapter
    private lateinit var emptyView: LinearLayout
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var btnAgregarPrimerContacto: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var usuario: String
    private val contactosList = mutableListOf<Contacto>()
    private val TAG = "VerContactosDebug" // Para logs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_contactos)

        // Obtener usuario con valor por defecto
        usuario = intent.getStringExtra("usuario") ?: run {
            // Intentar obtener de SharedPreferences
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            sharedPref.getString("usuario", "") ?: ""
        }

        Log.d(TAG, "Usuario obtenido: $usuario")

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        fabAgregar = findViewById(R.id.fabAgregar)
        btnAgregarPrimerContacto = findViewById(R.id.btnAgregarPrimerContacto)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)

        // Configurar toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactoAdapter(contactosList)
        recyclerView.adapter = adapter

        // Configurar botones
        fabAgregar.setOnClickListener {
            irARegistroContacto()
        }

        btnAgregarPrimerContacto.setOnClickListener {
            irARegistroContacto()
        }

        obtenerContactos()
    }

    private fun irARegistroContacto() {
        val intent = Intent(this, RegistroContactoActivity::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }

    private fun obtenerContactos() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("usuario", usuario)
        }

        val url = "${Config.URL}obtener_contactos.php"
        Log.d(TAG, "URL: $url")
        Log.d(TAG, "Enviando usuario: $usuario")

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                progressBar.visibility = View.GONE
                Log.d(TAG, "Respuesta recibida: ${response.toString()}")

                try {
                    if (response.getBoolean("success")) {
                        contactosList.clear()
                        val jsonArray: JSONArray = response.getJSONArray("contactos")
                        Log.d(TAG, "Número de contactos: ${jsonArray.length()}")

                        for (i in 0 until jsonArray.length()) {
                            val contactoJson = jsonArray.getJSONObject(i)
                            Log.d(TAG, "Procesando contacto $i: ${contactoJson.toString()}")

                            val contacto = Contacto(
                                codigo = contactoJson.getString("codigo"),
                                nombre = contactoJson.getString("nombre"),
                                direccion = contactoJson.optString("direccion", ""),
                                telefono = contactoJson.getString("telefono"),
                                correo = contactoJson.optString("correo", ""),
                                imagen = contactoJson.optString("imagen", ""),
                                usuario = contactoJson.optString("usuario", usuario)
                            )
                            contactosList.add(contacto)
                        }

                        adapter.notifyDataSetChanged()

                        if (contactosList.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            Toast.makeText(this, "No hay contactos", Toast.LENGTH_SHORT).show()
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            emptyView.visibility = View.GONE
                            Toast.makeText(this, "${contactosList.size} contactos cargados", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        val errorMsg = response.optString("message", "Error desconocido")
                        Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error del servidor: $errorMsg")
                    }
                } catch (e: Exception) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Toast.makeText(this, "Error al procesar datos: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Excepción: ${e.message}")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                progressBar.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

                val errorMsg = error.message ?: "Error de conexión"
                Toast.makeText(this, "Error de red: $errorMsg", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error Volley: $errorMsg")

                // Mostrar detalles adicionales del error
                error.networkResponse?.let {
                    Log.e(TAG, "Código HTTP: ${it.statusCode}")
                    Log.e(TAG, "Datos: ${String(it.data)}")
                }
            }
        )

        queue.add(request)
    }

    override fun onResume() {
        super.onResume()
        obtenerContactos()
    }
}