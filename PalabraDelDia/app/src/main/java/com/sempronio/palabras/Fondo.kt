package com.sempronio.palabras

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.min

// Genera la imagen (fondo pergamino + marco + texto) y la fija en el lock screen.
object Fondo {

    fun aplicar(ctx: Context, palabra: Palabra) {
        val (w, h) = tamanoPantalla(ctx)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        dibujarFondo(c, w, h)
        dibujarMarco(ctx, c, w, h)
        dibujarTexto(c, w, h, palabra)

        val wm = WallpaperManager.getInstance(ctx)
        // FLAG_LOCK = solo pantalla de bloqueo
        wm.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
    }

    private fun tamanoPantalla(ctx: Context): Pair<Int, Int> {
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)
        return Pair(dm.widthPixels, dm.heightPixels)
    }

    private fun dibujarFondo(c: Canvas, w: Int, h: Int) {
        // Degradado pergamino oscuro, vertical
        val p = Paint()
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
            Color.rgb(30, 26, 22), Color.rgb(18, 15, 12), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    }

    private fun dibujarMarco(ctx: Context, c: Canvas, w: Int, h: Int) {
        try {
            val marco = BitmapFactory.decodeResource(ctx.resources, R.drawable.marco)
            if (marco != null) {
                val dst = Rect(0, 0, w, h)
                c.drawBitmap(marco, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
                return
            }
        } catch (_: Exception) {}
        // Respaldo: marco simple por codigo si no carga el PNG
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.color = Color.rgb(198, 166, 100)
        p.strokeWidth = w * 0.02f
        val m = w * 0.06f
        c.drawRect(m, m, w - m, h - m, p)
    }

    private fun dibujarTexto(c: Canvas, w: Int, h: Int, palabra: Palabra) {
        val oro = Color.rgb(224, 200, 140)
        val cx = w / 2f
        val cyBase = h * 0.46f  // algo por encima de la mitad exacta

        // Termino grande, serif
        val pT = Paint(Paint.ANTI_ALIAS_FLAG)
        pT.color = oro
        pT.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        pT.textAlign = Paint.Align.CENTER
        pT.textSize = ajustarTamano(pT, palabra.termino, w * 0.78f, w * 0.11f)
        c.drawText(palabra.termino, cx, cyBase, pT)

        // Linea divisoria
        val pL = Paint(Paint.ANTI_ALIAS_FLAG)
        pL.color = Color.argb(140, 198, 166, 100)
        pL.strokeWidth = 3f
        val lw = w * 0.22f
        c.drawLine(cx - lw, cyBase + h * 0.03f, cx + lw, cyBase + h * 0.03f, pL)

        // Definicion, mas pequena, con salto de linea automatico
        val pD = Paint(Paint.ANTI_ALIAS_FLAG)
        pD.color = Color.rgb(210, 198, 178)
        pD.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        pD.textAlign = Paint.Align.CENTER
        pD.textSize = w * 0.045f

        val lineas = envolver(pD, palabra.definicion, w * 0.72f)
        var y = cyBase + h * 0.075f
        val salto = pD.textSize * 1.35f
        for (linea in lineas) {
            c.drawText(linea, cx, y, pD)
            y += salto
        }
    }

    // Reduce el tamano de fuente si el texto no cabe en el ancho dado
    private fun ajustarTamano(p: Paint, texto: String, anchoMax: Float, inicial: Float): Float {
        var t = inicial
        p.textSize = t
        while (p.measureText(texto) > anchoMax && t > 20f) {
            t -= 2f
            p.textSize = t
        }
        return t
    }

    // Parte la definicion en varias lineas segun el ancho
    private fun envolver(p: Paint, texto: String, anchoMax: Float): List<String> {
        val palabras = texto.split(" ")
        val lineas = mutableListOf<String>()
        var actual = StringBuilder()
        for (pal in palabras) {
            val prueba = if (actual.isEmpty()) pal else "$actual $pal"
            if (p.measureText(prueba) <= anchoMax) {
                actual = StringBuilder(prueba)
            } else {
                if (actual.isNotEmpty()) lineas.add(actual.toString())
                actual = StringBuilder(pal)
            }
        }
        if (actual.isNotEmpty()) lineas.add(actual.toString())
        return lineas
    }
}
