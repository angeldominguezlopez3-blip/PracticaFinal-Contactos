package com.example.practicafinal_contactos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class RegistroContactoActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var etNombre: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnActualizar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var usuario: String

    private val PICK_IMAGE_REQUEST = 1
    private var imagenBase64: String = ""
    private var bitmapImagen: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_contacto)

        usuario = intent.getStringExtra("usuario") ?: ""

        inicializarVistas()
        configurarEventos()
    }

    private fun inicializarVistas() {
        etCodigo = findViewById(R.id.etCodigo)
        etNombre = findViewById(R.id.etNombre)
        etDireccion = findViewById(R.id.etDireccion)
        etTelefono = findViewById(R.id.etTelefono)
        etCorreo = findViewById(R.id.etCorreo)
        ivFoto = findViewById(R.id.ivFoto)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnActualizar = findViewById(R.id.btnActualizar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnLimpiar = findViewById(R.id.btnLimpiar)
    }

    private fun configurarEventos() {
        btnSeleccionarFoto.setOnClickListener {
            seleccionarImagen()
        }

        btnGuardar.setOnClickListener {
            if (validarCampos()) {
                guardarContacto()
            }
        }

        btnActualizar.setOnClickListener {
            if (etCodigo.text.toString().isEmpty()) {
                Toast.makeText(this, "Ingrese el código del contacto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (validarCampos()) {
                actualizarContacto()
            }
        }

        btnEliminar.setOnClickListener {
            if (etCodigo.text.toString().isEmpty()) {
                Toast.makeText(this, "Ingrese el código del contacto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            eliminarContacto()
        }

        btnLimpiar.setOnClickListener {
            limpiarCampos()
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri: Uri = data.data!!
            try {
                bitmapImagen = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                ivFoto.setImageBitmap(bitmapImagen)
                imagenBase64 = bitmapToBase64(bitmapImagen!!)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun validarCampos(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (telefono.isEmpty()) {
            Toast.makeText(this, "El teléfono es requerido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun guardarContacto() {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("nombre", etNombre.text.toString().trim())
            put("direccion", etDireccion.text.toString().trim())
            put("telefono", etTelefono.text.toString().trim())
            put("correo", etCorreo.text.toString().trim())
            put("imagen", imagenBase64)
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}guardar_contacto.php",
            jsonObject,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
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

    private fun actualizarContacto() {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("codigo", etCodigo.text.toString().trim())
            put("nombre", etNombre.text.toString().trim())
            put("direccion", etDireccion.text.toString().trim())
            put("telefono", etTelefono.text.toString().trim())
            put("correo", etCorreo.text.toString().trim())
            put("imagen", imagenBase64)
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}actualizar_contacto.php",
            jsonObject,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Contacto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
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

    private fun eliminarContacto() {
        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("codigo", etCodigo.text.toString().trim())
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}eliminar_contacto.php",
            jsonObject,
            Response.Listener { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Contacto eliminado exitosamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
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

    private fun limpiarCampos() {
        etCodigo.text.clear()
        etNombre.text.clear()
        etDireccion.text.clear()
        etTelefono.text.clear()
        etCorreo.text.clear()
        ivFoto.setImageResource(R.drawable.ic_default_contact)
        imagenBase64 = ""
        bitmapImagen = null
    }
}