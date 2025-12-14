package com.example.practicafinal_contactos

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class RegistroContactoActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var etNombre: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var ivFoto: CircleImageView
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnActualizar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnLimpiar: Button
    private lateinit var btnBuscar: Button
    private lateinit var usuario: String
    private lateinit var progressDialog: ProgressDialog

    private val PICK_IMAGE_REQUEST = 1
    private var imagenUrl: String = ""
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
        btnBuscar = findViewById(R.id.btnBuscar)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Por favor espere...")
        progressDialog.setCancelable(false)
    }

    private fun configurarEventos() {
        btnSeleccionarFoto.setOnClickListener {
            seleccionarImagen()
        }

        btnBuscar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es requerido para buscar"
                etNombre.requestFocus()
                Toast.makeText(this, "Ingrese el nombre del contacto a buscar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buscarContactoPorNombre(nombre)
        }

        btnGuardar.setOnClickListener {
            if (validarCampos()) {
                if (bitmapImagen != null) {
                    // Si hay imagen, primero subirla a ImgBB
                    subirImagenAImgBB()
                } else {
                    // Si no hay imagen, guardar directamente
                    guardarContacto("")
                }
            }
        }

        btnActualizar.setOnClickListener {
            if (etCodigo.text.toString().isEmpty()) {
                Toast.makeText(this, "Primero busque el contacto para actualizarlo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (validarCampos()) {
                if (bitmapImagen != null && imagenUrl.isEmpty()) {
                    // Si hay nueva imagen, subirla primero
                    subirImagenAImgBB(esActualizacion = true)
                } else {
                    // Si no hay nueva imagen o ya tiene URL, actualizar directamente
                    actualizarContacto(imagenUrl)
                }
            }
        }

        btnEliminar.setOnClickListener {
            if (etCodigo.text.toString().isEmpty()) {
                Toast.makeText(this, "Primero busque el contacto para eliminarlo", Toast.LENGTH_SHORT).show()
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
                // Redimensionar la imagen para optimizar
                bitmapImagen = redimensionarImagen(bitmapImagen!!, 800, 800)
                ivFoto.setImageBitmap(bitmapImagen)
                imagenUrl = "" // Resetear URL porque es una nueva imagen
                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redimensionarImagen(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun subirImagenAImgBB(esActualizacion: Boolean = false) {
        if (bitmapImagen == null) {
            Toast.makeText(this, "No hay imagen seleccionada", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Subiendo imagen...")
        progressDialog.show()

        val queue = Volley.newRequestQueue(this)
        val imageBase64 = bitmapToBase64(bitmapImagen!!)

        val request = object : StringRequest(
            Method.POST,
            Config.IMGBB_UPLOAD_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONObject("data")
                        imagenUrl = data.getString("url")

                        progressDialog.dismiss()
                        Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show()

                        // Guardar o actualizar contacto con la URL de la imagen
                        if (esActualizacion) {
                            actualizarContacto(imagenUrl)
                        } else {
                            guardarContacto(imagenUrl)
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al procesar respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["key"] = Config.IMGBB_API_KEY
                params["image"] = imageBase64
                return params
            }
        }

        queue.add(request)
    }

    private fun buscarContactoPorNombre(nombre: String) {
        progressDialog.setMessage("Buscando contacto...")
        progressDialog.show()

        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("nombre", nombre)
            put("usuario", usuario)
        }

        // Para DEBUG: Mostrar lo que se está enviando
        Log.d("BuscarContacto", "Buscando: $nombre para usuario: $usuario")
        Log.d("BuscarContacto", "JSON enviado: ${jsonObject.toString()}")

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}buscar_contacto.php",
            jsonObject,
            { response ->
                progressDialog.dismiss()

                // DEBUG: Mostrar respuesta completa
                Log.d("BuscarContacto", "Respuesta recibida: $response")

                try {
                    val success = response.getBoolean("success")

                    if (success) {
                        val contacto = response.getJSONObject("contacto")

                        // DEBUG: Mostrar datos del contacto
                        Log.d("BuscarContacto", "Contacto encontrado: ${contacto.toString()}")

                        // Llenar los campos con los datos del contacto
                        val id = contacto.optString("id", "")
                        val codigo = contacto.optString("codigo", "")

                        // Usar id si codigo está vacío, o viceversa
                        val codigoMostrar = if (codigo.isNotEmpty()) codigo else id
                        etCodigo.setText(codigoMostrar)

                        etNombre.setText(contacto.optString("nombre", ""))
                        etDireccion.setText(contacto.optString("direccion", ""))
                        etTelefono.setText(contacto.optString("telefono", ""))
                        etCorreo.setText(contacto.optString("correo", ""))

                        // Cargar la imagen si existe
                        imagenUrl = contacto.optString("imagen", "")
                        if (imagenUrl.isNotEmpty() && imagenUrl != "null") {
                            Picasso.get()
                                .load(imagenUrl)
                                .placeholder(R.drawable.ic_default_contact)
                                .error(R.drawable.ic_default_contact)
                                .into(ivFoto, object : com.squareup.picasso.Callback {
                                    override fun onSuccess() {
                                        Log.d("BuscarContacto", "Imagen cargada exitosamente")
                                    }
                                    override fun onError(e: Exception?) {
                                        Log.e("BuscarContacto", "Error al cargar imagen: ${e?.message}")
                                        ivFoto.setImageResource(R.drawable.ic_default_contact)
                                    }
                                })
                        } else {
                            ivFoto.setImageResource(R.drawable.ic_default_contact)
                            Log.d("BuscarContacto", "No hay imagen para este contacto")
                        }

                        Toast.makeText(this, "Contacto encontrado", Toast.LENGTH_SHORT).show()

                    } else {
                        val mensaje = response.optString("message", "Contacto no encontrado")
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                        Log.d("BuscarContacto", "No se encontró contacto: $mensaje")

                        // Limpiar campos si no se encontró
                        etCodigo.text.clear()
                        etDireccion.text.clear()
                        etTelefono.text.clear()
                        etCorreo.text.clear()
                        ivFoto.setImageResource(R.drawable.ic_default_contact)
                        imagenUrl = ""
                    }
                } catch (e: Exception) {
                    Log.e("BuscarContacto", "Error al procesar respuesta: ${e.message}")
                    Toast.makeText(this, "Error al procesar respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressDialog.dismiss()
                Log.e("BuscarContacto", "Error Volley: ${error.message}")
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )



        queue.add(request)
    }

    private fun validarCampos(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()

        if (nombre.isEmpty()) {
            etNombre.error = "El nombre es requerido"
            etNombre.requestFocus()
            Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (telefono.isEmpty()) {
            etTelefono.error = "El teléfono es requerido"
            etTelefono.requestFocus()
            Toast.makeText(this, "El teléfono es requerido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun guardarContacto(urlImagen: String) {
        progressDialog.setMessage("Guardando contacto...")
        progressDialog.show()

        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("nombre", etNombre.text.toString().trim())
            put("direccion", etDireccion.text.toString().trim())
            put("telefono", etTelefono.text.toString().trim())
            put("correo", etCorreo.text.toString().trim())
            put("imagen", urlImagen)
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}guardar_contacto.php",
            jsonObject,
            { response ->
                progressDialog.dismiss()
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    private fun actualizarContacto(urlImagen: String) {
        progressDialog.setMessage("Actualizando contacto...")
        progressDialog.show()

        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("codigo", etCodigo.text.toString().trim())
            put("nombre", etNombre.text.toString().trim())
            put("direccion", etDireccion.text.toString().trim())
            put("telefono", etTelefono.text.toString().trim())
            put("correo", etCorreo.text.toString().trim())
            put("imagen", urlImagen)
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}actualizar_contacto.php",
            jsonObject,
            { response ->
                progressDialog.dismiss()
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Contacto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    private fun eliminarContacto() {
        progressDialog.setMessage("Eliminando contacto...")
        progressDialog.show()

        val queue = Volley.newRequestQueue(this)
        val jsonObject = JSONObject().apply {
            put("codigo", etCodigo.text.toString().trim())
            put("usuario", usuario)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${Config.URL}eliminar_contacto.php",
            jsonObject,
            { response ->
                progressDialog.dismiss()
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Contacto eliminado exitosamente", Toast.LENGTH_SHORT).show()
                    limpiarCampos()
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressDialog.dismiss()
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
        imagenUrl = ""
        bitmapImagen = null
    }
}