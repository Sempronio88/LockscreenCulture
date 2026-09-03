package com.sempronio.palabras

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Worker que se ejecuta a diario y cambia la palabra si es un dia nuevo.
class TareaDiaria(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        try {
            val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val ultimo = Almacen.cargarUltimoDia(applicationContext)
            // Solo avanza si es un dia distinto al ultimo aplicado
            val avanzar = hoy != ultimo
            val palabra = Seleccion.palabraActual(applicationContext, avanzar) ?: return Result.success()
            Fondo.aplicar(applicationContext, palabra)
            if (avanzar) Almacen.guardarUltimoDia(applicationContext, hoy)
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    companion object {
        private const val NOMBRE = "tarea_palabra_diaria"

        fun programar(ctx: Context) {
            // Se repite cada ~6h; dentro comprueba si el dia cambio.
            // Frecuencia alta para compensar que HyperOS puede saltarse ejecuciones.
            val req = PeriodicWorkRequestBuilder<TareaDiaria>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                NOMBRE, ExistingPeriodicWorkPolicy.UPDATE, req
            )
        }

        // Refresco inmediato (al abrir la app o pulsar boton)
        fun ejecutarYa(ctx: Context) {
            val req = OneTimeWorkRequestBuilder<TareaDiaria>().build()
            WorkManager.getInstance(ctx).enqueue(req)
        }
    }
}
