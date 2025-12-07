package com.example.practicafinal_contactos

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_contactos)

        usuario = intent.getStringExtra("usuario") ?: ""

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

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}obtener_contactos.php",
            jsonObject,
            Response.Listener { response ->
                progressBar.visibility = View.GONE

                if (response.getBoolean("success")) {
                    contactosList.clear()
                    val jsonArray: JSONArray = response.getJSONArray("contactos")

                    for (i in 0 until jsonArray.length()) {
                        val contactoJson = jsonArray.getJSONObject(i)
                        val contacto = Contacto(
                            codigo = contactoJson.getInt("codigo"),
                            nombre = contactoJson.getString("nombre"),
                            direccion = contactoJson.optString("direccion", ""),
                            telefono = contactoJson.getString("telefono"),
                            correo = contactoJson.optString("correo", ""),
                            imagen = contactoJson.optString("imagen", ""),
                            usuario = contactoJson.getString("usuario")
                        )
                        contactosList.add(contacto)
                    }

                    adapter.notifyDataSetChanged()

                    if (contactosList.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                    }
                } else {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            },
            Response.ErrorListener { error ->
                progressBar.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
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