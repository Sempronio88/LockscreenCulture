package com.sempronio.palabras

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sempronio.palabras.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        Semilla.sembrarSiVacio(this)
        TareaDiaria.programar(this)

        b.btnCategorias.setOnClickListener {
            startActivity(android.content.Intent(this, CategoriasActivity::class.java))
        }
        b.btnPalabras.setOnClickListener {
            startActivity(android.content.Intent(this, PalabrasActivity::class.java))
        }
        b.btnAplicarAhora.setOnClickListener {
            val palabra = Seleccion.palabraActual(this, avanzar = false)
            if (palabra == null) {
                Toast.makeText(this, "No hay palabras en las categorías activas", Toast.LENGTH_LONG).show()
            } else {
                try {
                    Fondo.aplicar(this, palabra)
                    Toast.makeText(this, "Aplicado: ${palabra.termino}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al fijar el fondo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        b.btnSiguiente.setOnClickListener {
            val palabra = Seleccion.palabraActual(this, avanzar = true)
            if (palabra != null) {
                try {
                    Fondo.aplicar(this, palabra)
                    Toast.makeText(this, "Nueva: ${palabra.termino}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca por si cambio el dia mientras la app estaba cerrada
        TareaDiaria.ejecutarYa(this)
        mostrarActual()
    }

    private fun mostrarActual() {
        val palabra = Seleccion.palabraActual(this, avanzar = false)
        if (palabra != null) {
            b.txtActual.text = palabra.termino
            b.txtDefinicion.text = palabra.definicion
        } else {
            b.txtActual.text = "—"
            b.txtDefinicion.text = "Sin palabras activas"
        }
    }
}
