package com.sempronio.palabras

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sempronio.palabras.databinding.ActivityListaBinding

class PalabrasActivity : AppCompatActivity() {
    private lateinit var b: ActivityListaBinding
    private lateinit var palabras: MutableList<Palabra>
    private lateinit var cats: MutableList<Categoria>

    // Selector de archivos del sistema
    private val abrirArchivo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> if (uri != null) elegirCategoriaEImportar(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListaBinding.inflate(layoutInflater)
        setContentView(b.root)
        title = "Palabras"
        b.txtInfo.text = "Toca una palabra para editarla. Usa 'Importar' para añadir en lote."

        palabras = Almacen.cargarPalabras(this)
        cats = Almacen.cargarCategorias(this)
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = Adaptador()

        b.btnAnadir.text = "Añadir palabra"
        b.btnAnadir.setOnClickListener { dialogoEditar(null) }

        b.btnImportar.visibility = android.view.View.VISIBLE
        b.btnImportar.text = "Importar archivo"
        b.btnImportar.setOnClickListener {
            abrirArchivo.launch(arrayOf("text/plain", "text/comma-separated-values", "text/csv", "*/*"))
        }
    }

    private fun persistir() { Almacen.guardarTodo(this, cats, palabras) }

    private fun elegirCategoriaEImportar(uri: Uri) {
        if (cats.isEmpty()) {
            Toast.makeText(this, "Crea primero una categoría", Toast.LENGTH_LONG).show()
            return
        }
        val nombres = cats.map { it.nombre }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("¿A qué categoría añadir?")
            .setItems(nombres) { _, which ->
                val catId = cats[which].id
                try {
                    val r = Importador.importar(this, uri, catId)
                    palabras = Almacen.cargarPalabras(this)
                    b.recycler.adapter = Adaptador()
                    AlertDialog.Builder(this)
                        .setTitle("Importación terminada")
                        .setMessage("Añadidas: ${r.anadidas}\nActualizadas: ${r.actualizadas}\nIgnoradas: ${r.ignoradas}")
                        .setPositiveButton("Vale", null)
                        .show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al importar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun dialogoEditar(existente: Palabra?) {
        if (cats.isEmpty()) {
            Toast.makeText(this, "Crea primero una categoría", Toast.LENGTH_LONG).show()
            return
        }
        val cont = LinearLayout(this)
        cont.orientation = LinearLayout.VERTICAL
        cont.setPadding(48, 24, 48, 0)

        val eTermino = EditText(this); eTermino.hint = "Palabra"
        eTermino.setText(existente?.termino ?: "")
        val eDef = EditText(this); eDef.hint = "Definición"
        eDef.setText(existente?.definicion ?: "")

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cats.map { it.nombre })
        val idxSel = cats.indexOfFirst { it.id == existente?.categoriaId }
        if (idxSel >= 0) spinner.setSelection(idxSel)

        cont.addView(TextView(this).apply { text = "Categoría:" })
        cont.addView(spinner); cont.addView(eTermino); cont.addView(eDef)

        val builder = AlertDialog.Builder(this)
            .setTitle(if (existente == null) "Nueva palabra" else "Editar palabra")
            .setView(cont)
            .setPositiveButton("Guardar") { _, _ ->
                val t = eTermino.text.toString().trim()
                val dfn = eDef.text.toString().trim()
                val catId = cats[spinner.selectedItemPosition].id
                if (t.isNotEmpty()) {
                    if (existente == null) palabras.add(Palabra(Almacen.nuevoId(), t, dfn, catId))
                    else {
                        val i = palabras.indexOfFirst { it.id == existente.id }
                        if (i >= 0) palabras[i] = existente.copy(termino = t, definicion = dfn, categoriaId = catId)
                    }
                    persistir(); b.recycler.adapter?.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancelar", null)
        if (existente != null) {
            builder.setNeutralButton("Borrar") { _, _ ->
                palabras.removeAll { it.id == existente.id }
                persistir(); b.recycler.adapter?.notifyDataSetChanged()
            }
        }
        builder.show()
    }

    inner class Adaptador : RecyclerView.Adapter<Adaptador.VH>() {
        inner class VH(val v: LinearLayout) : RecyclerView.ViewHolder(v)
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val fila = LinearLayout(this@PalabrasActivity)
            fila.orientation = LinearLayout.VERTICAL
            fila.setPadding(40, 28, 40, 28)
            val t = TextView(this@PalabrasActivity); t.id = 2001; t.textSize = 18f
            val d = TextView(this@PalabrasActivity); d.id = 2002; d.textSize = 13f
            d.setTextColor(0xFF777777.toInt())
            val cat = TextView(this@PalabrasActivity); cat.id = 2003; cat.textSize = 11f
            cat.setTextColor(0xFFAA8844.toInt())
            fila.addView(t); fila.addView(d); fila.addView(cat)
            return VH(fila)
        }
        override fun getItemCount() = palabras.size
        override fun onBindViewHolder(h: VH, position: Int) {
            val p = palabras[position]
            h.v.findViewById<TextView>(2001).text = p.termino
            h.v.findViewById<TextView>(2002).text = p.definicion
            h.v.findViewById<TextView>(2003).text =
                cats.firstOrNull { it.id == p.categoriaId }?.nombre ?: "?"
            h.v.setOnClickListener { dialogoEditar(p) }
        }
    }
}
