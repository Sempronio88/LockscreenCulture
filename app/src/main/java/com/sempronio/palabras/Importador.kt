package com.sempronio.palabras

import android.content.Context
import android.net.Uri

// Lee un archivo de texto con lineas "termino | definicion" y las
// incorpora a una categoria dada. Anade nuevas y actualiza existentes.
object Importador {

    data class Resultado(val anadidas: Int, val actualizadas: Int, val ignoradas: Int)

    fun importar(ctx: Context, uri: Uri, categoriaId: Long): Resultado {
        val texto = ctx.contentResolver.openInputStream(uri)?.use {
            it.bufferedReader().readText()
        } ?: return Resultado(0, 0, 0)

        val palabras = Almacen.cargarPalabras(ctx)
        val categorias = Almacen.cargarCategorias(ctx)
        var anadidas = 0; var actualizadas = 0; var ignoradas = 0

        for (lineaRaw in texto.lines()) {
            val linea = lineaRaw.trim()
            if (linea.isEmpty() || linea.startsWith("#")) continue

            // separadores admitidos: | ; tab
            val sep = when {
                linea.contains("|") -> "|"
                linea.contains("\t") -> "\t"
                linea.contains(";") -> ";"
                else -> null
            }
            if (sep == null) { ignoradas++; continue }

            val partes = linea.split(sep, limit = 2)
            val termino = partes[0].trim()
            var definicion = partes.getOrElse(1) { "" }.trim()
            // quitar marcador (RAE)/(pop.) del final si existe
            definicion = definicion.replace(Regex("\\s*\\((RAE|pop\\.)\\)\\s*$"), "").trim()
            if (termino.isEmpty()) { ignoradas++; continue }

            val existente = palabras.indexOfFirst {
                it.termino.equals(termino, ignoreCase = true) && it.categoriaId == categoriaId
            }
            if (existente >= 0) {
                palabras[existente] = palabras[existente].copy(definicion = definicion)
                actualizadas++
            } else {
                palabras.add(Palabra(Almacen.nuevoId(), termino, definicion, categoriaId))
                anadidas++
            }
        }

        Almacen.guardarTodo(ctx, categorias, palabras)
        return Resultado(anadidas, actualizadas, ignoradas)
    }
}
