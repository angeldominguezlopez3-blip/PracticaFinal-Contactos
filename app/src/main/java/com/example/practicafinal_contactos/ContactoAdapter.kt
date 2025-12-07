package com.example.practicafinal_contactos

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ContactoAdapter(private val contactos: List<Contacto>) :
    RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder>() {

    class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoto: CircleImageView = itemView.findViewById(R.id.ivFotoContacto)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreContacto)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefonoContacto)
        val tvCorreo: TextView = itemView.findViewById(R.id.tvCorreoContacto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacto, parent, false)
        return ContactoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contacto = contactos[position]

        holder.tvNombre.text = contacto.nombre
        holder.tvTelefono.text = contacto.telefono
        holder.tvCorreo.text = contacto.correo

        // Cargar imagen
        if (contacto.imagen.isNotEmpty()) {
            try {
                val decodedString = Base64.decode(contacto.imagen, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                holder.ivFoto.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivFoto.setImageResource(R.drawable.ic_default_contact)
            }
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_default_contact)
        }
    }

    override fun getItemCount(): Int = contactos.size
}