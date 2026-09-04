package com.sempronio.palabras

import android.content.Context

// Datos iniciales la primera vez que se abre la app.
object Semilla {
    fun sembrarSiVacio(ctx: Context) {
        val cats = Almacen.cargarCategorias(ctx)
        if (cats.isNotEmpty()) return

        val catInsultos = Categoria(1L, "Insultos en desuso", true)
        val categorias = mutableListOf(catInsultos)

        val p = mutableListOf<Palabra>()
        var id = 100L
        fun add(t: String, d: String) { p.add(Palabra(id++, t, d, catInsultos.id)) }

        add("Bellaco", "Persona ruin y de baja condición; malvado o pícaro.")
        add("Mequetrefe", "Persona entrometida y de poco provecho que presume sin mérito.")
        add("Zascandil", "Hombre despreciable, enredador y de poco seso.")
        add("Follón", "Cobarde y vil; también alborotador amigo de broncas.")
        add("Gaznápiro", "Simplón que se queda embobado; palurdo.")
        add("Truhán", "Embaucador sin vergüenza que engaña para su provecho.")
        add("Fementido", "Falso, mentiroso, que falta a la palabra dada.")
        add("Cenutrio", "Tonto de remate; hombre de cortísimo entendimiento.")
        add("Zafio", "Grosero, tosco y sin modales.")
        add("Malandrín", "Bellaco perverso; salteador de caminos.")
        add("Petimetre", "Presumido ridículo, obsesionado con su apariencia.")
        add("Rufián", "Hombre de mala vida, canalla y sin honra.")
        add("Mentecato", "Necio de escaso juicio; falto de razón.")
        add("Baldragas", "Hombre flojo y sin carácter; pusilánime.")
        add("Mandria", "Holgazán apático, sin ánimo ni ganas de nada.")

        Almacen.guardarTodo(ctx, categorias, p)
    }
}
