package com.sempronio.palabras

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Una palabra pertenece a una categoria
data class Palabra(
    val id: Long,
    val termino: String,
    val definicion: String,
    val categoriaId: Long
)

data class Categoria(
    val id: Long,
    val nombre: String,
    var activa: Boolean   // si esta seleccionada para mostrarse
)

// Almacen simple en un fichero JSON dentro de la app.
// Nada de bases de datos complejas: para el volumen esperado sobra.
object Almacen {
    private const val FICHERO = "datos.json"

    private fun leerRaw(ctx: Context): JSONObject {
        return try {
            val txt = ctx.openFileInput(FICHERO).bufferedReader().use { it.readText() }
            if (txt.isBlank()) JSONObject() else JSONObject(txt)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun guardarRaw(ctx: Context, obj: JSONObject) {
        ctx.openFileOutput(FICHERO, Context.MODE_PRIVATE).use {
            it.write(obj.toString().toByteArray())
        }
    }

    fun cargarCategorias(ctx: Context): MutableList<Categoria> {
        val obj = leerRaw(ctx)
        val arr = obj.optJSONArray("categorias") ?: JSONArray()
        val lista = mutableListOf<Categoria>()
        for (i in 0 until arr.length()) {
            val c = arr.getJSONObject(i)
            lista.add(Categoria(c.getLong("id"), c.getString("nombre"), c.getBoolean("activa")))
        }
        return lista
    }

    fun cargarPalabras(ctx: Context): MutableList<Palabra> {
        val obj = leerRaw(ctx)
        val arr = obj.optJSONArray("palabras") ?: JSONArray()
        val lista = mutableListOf<Palabra>()
        for (i in 0 until arr.length()) {
            val p = arr.getJSONObject(i)
            lista.add(Palabra(p.getLong("id"), p.getString("termino"),
                p.getString("definicion"), p.getLong("categoriaId")))
        }
        return lista
    }

    fun guardarTodo(ctx: Context, categorias: List<Categoria>, palabras: List<Palabra>) {
        val obj = JSONObject()
        val catArr = JSONArray()
        for (c in categorias) {
            catArr.put(JSONObject().apply {
                put("id", c.id); put("nombre", c.nombre); put("activa", c.activa)
            })
        }
        val palArr = JSONArray()
        for (p in palabras) {
            palArr.put(JSONObject().apply {
                put("id", p.id); put("termino", p.termino)
                put("definicion", p.definicion); put("categoriaId", p.categoriaId)
            })
        }
        obj.put("categorias", catArr)
        obj.put("palabras", palArr)
        guardarRaw(ctx, obj)
    }

    // Estado de la baraja: orden aleatorio de ids ya generado y posicion actual.
    fun guardarBaraja(ctx: Context, orden: List<Long>, pos: Int, firma: String) {
        val obj = leerRaw(ctx)
        obj.put("barajaOrden", JSONArray(orden))
        obj.put("barajaPos", pos)
        obj.put("barajaFirma", firma)
        guardarRaw(ctx, obj)
    }

    fun cargarBarajaOrden(ctx: Context): List<Long> {
        val arr = leerRaw(ctx).optJSONArray("barajaOrden") ?: return emptyList()
        return (0 until arr.length()).map { arr.getLong(it) }
    }

    fun cargarBarajaPos(ctx: Context): Int = leerRaw(ctx).optInt("barajaPos", 0)
    fun cargarBarajaFirma(ctx: Context): String = leerRaw(ctx).optString("barajaFirma", "")

    fun guardarUltimoDia(ctx: Context, dia: String) {
        val obj = leerRaw(ctx); obj.put("ultimoDia", dia); guardarRaw(ctx, obj)
    }
    fun cargarUltimoDia(ctx: Context): String = leerRaw(ctx).optString("ultimoDia", "")

    fun nuevoId(): Long = System.currentTimeMillis() + (Math.random() * 1000).toLong()
}
