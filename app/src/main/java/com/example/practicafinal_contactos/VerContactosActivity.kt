package com.example.practicafinal_contactos

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class VerContactosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactoAdapter
    private lateinit var usuario: String
    private val contactosList = mutableListOf<Contacto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_contactos)

        usuario = intent.getStringExtra("usuario") ?: ""

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ContactoAdapter(contactosList)
        recyclerView.adapter = adapter

        obtenerContactos()
    }

    private fun obtenerContactos() {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}obtener_contactos.php",
            jsonObject,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    contactosList.clear()
                    val jsonArray: JSONArray = response.getJSONArray("contactos")

                    for (i in 0 until jsonArray.length()) {
                        val contactoJson = jsonArray.getJSONObject(i)
                        val contacto = Contacto(
                            codigo = contactoJson.getInt("codigo"),
                            nombre = contactoJson.getString("nombre"),
                            direccion = contactoJson.getString("direccion"),
                            telefono = contactoJson.getString("telefono"),
                            correo = contactoJson.getString("correo"),
                            imagen = contactoJson.getString("imagen"),
                            usuario = contactoJson.getString("usuario")
                        )
                        contactosList.add(contacto)
                    }

                    adapter.notifyDataSetChanged()

                    if (contactosList.isEmpty()) {
                        Toast.makeText(this, "No hay contactos registrados", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    override fun onResume() {
        super.onResume()
        obtenerContactos()
    }
}