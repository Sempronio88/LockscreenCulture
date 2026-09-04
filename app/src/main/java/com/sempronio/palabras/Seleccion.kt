package com.sempronio.palabras

import android.content.Context

// Logica de la baraja: coge palabras de las categorias activas,
// las baraja una vez, las recorre sin repetir, y al agotarse rebaraja.
object Seleccion {

    // "firma" identifica el conjunto activo. Si cambian las categorias activas
    // o las palabras, la firma cambia y se rebaraja desde cero.
    private fun firma(palabras: List<Palabra>, categorias: List<Categoria>): String {
        val activas = categorias.filter { it.activa }.map { it.id }.sorted()
        val ids = palabras.filter { it.categoriaId in activas }.map { it.id }.sorted()
        return activas.joinToString(",") + "|" + ids.joinToString(",")
    }

    // Devuelve la palabra que toca HOY. avanzar=true pasa a la siguiente.
    fun palabraActual(ctx: Context, avanzar: Boolean): Palabra? {
        val palabras = Almacen.cargarPalabras(ctx)
        val categorias = Almacen.cargarCategorias(ctx)
        val activas = categorias.filter { it.activa }.map { it.id }.toSet()
        val pool = palabras.filter { it.categoriaId in activas }
        if (pool.isEmpty()) return null

        val firmaActual = firma(palabras, categorias)
        var orden = Almacen.cargarBarajaOrden(ctx)
        var pos = Almacen.cargarBarajaPos(ctx)
        val firmaGuardada = Almacen.cargarBarajaFirma(ctx)

        // Si el conjunto cambio, rebarajar
        if (firmaActual != firmaGuardada || orden.isEmpty()) {
            orden = pool.map { it.id }.shuffled()
            pos = 0
            Almacen.guardarBaraja(ctx, orden, pos, firmaActual)
        }

        if (avanzar) {
            pos += 1
            if (pos >= orden.size) {
                // agotada: rebarajar de nuevo
                orden = pool.map { it.id }.shuffled()
                pos = 0
            }
            Almacen.guardarBaraja(ctx, orden, pos, firmaActual)
        }

        val idActual = orden.getOrNull(pos) ?: orden.first()
        return pool.firstOrNull { it.id == idActual } ?: pool.first()
    }
}
