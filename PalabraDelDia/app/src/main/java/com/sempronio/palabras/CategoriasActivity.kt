package com.sempronio.palabras

import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sempronio.palabras.databinding.ActivityListaBinding

class CategoriasActivity : AppCompatActivity() {
    private lateinit var b: ActivityListaBinding
    private lateinit var cats: MutableList<Categoria>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListaBinding.inflate(layoutInflater)
        setContentView(b.root)
        title = "Categorías"
        b.txtInfo.text = "Marca las categorías que quieres mezclar en el bloqueo."

        cats = Almacen.cargarCategorias(this)
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = Adaptador()

        b.btnAnadir.text = "Añadir categoría"
        b.btnAnadir.setOnClickListener { dialogoNueva() }
    }

    private fun persistir() {
        val palabras = Almacen.cargarPalabras(this)
        Almacen.guardarTodo(this, cats, palabras)
    }

    private fun dialogoNueva() {
        val input = EditText(this)
        input.hint = "Nombre de la categoría"
        AlertDialog.Builder(this)
            .setTitle("Nueva categoría")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    cats.add(Categoria(Almacen.nuevoId(), nombre, true))
                    persistir()
                    b.recycler.adapter?.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    inner class Adaptador : RecyclerView.Adapter<Adaptador.VH>() {
        inner class VH(val v: LinearLayout) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val fila = LinearLayout(this@CategoriasActivity)
            fila.orientation = LinearLayout.HORIZONTAL
            fila.setPadding(32, 28, 32, 28)
            val check = CheckBox(this@CategoriasActivity)
            check.id = 1001
            val txt = TextView(this@CategoriasActivity)
            txt.id = 1002
            txt.textSize = 17f
            txt.setPadding(24, 0, 0, 0)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            txt.layoutParams = lp
            val borrar = TextView(this@CategoriasActivity)
            borrar.id = 1003
            borrar.text = "Borrar"
            borrar.setTextColor(0xFFB00020.toInt())
            fila.addView(check)
            fila.addView(txt)
            fila.addView(borrar)
            return VH(fila)
        }

        override fun getItemCount() = cats.size

        override fun onBindViewHolder(h: VH, position: Int) {
            val cat = cats[position]
            val check = h.v.findViewById<CheckBox>(1001)
            val txt = h.v.findViewById<TextView>(1002)
            val borrar = h.v.findViewById<TextView>(1003)
            check.setOnCheckedChangeListener(null)
            check.isChecked = cat.activa
            txt.text = cat.nombre
            check.setOnCheckedChangeListener { _, isChecked ->
                cat.activa = isChecked
                persistir()
            }
            borrar.setOnClickListener {
                AlertDialog.Builder(this@CategoriasActivity)
                    .setTitle("Borrar '${cat.nombre}'")
                    .setMessage("Se borrarán también sus palabras.")
                    .setPositiveButton("Borrar") { _, _ ->
                        val palabras = Almacen.cargarPalabras(this@CategoriasActivity)
                            .filter { it.categoriaId != cat.id }
                        cats.removeAt(position)
                        Almacen.guardarTodo(this@CategoriasActivity, cats, palabras)
                        notifyDataSetChanged()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }
}
