package com.sempronio.palabras

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.view.WindowManager

// Genera la imagen (fondo pergamino + marco IA + texto) y la fija en el lock screen.
object Fondo {

    fun aplicar(ctx: Context, palabra: Palabra) {
        val (w, h) = tamanoPantalla(ctx)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        dibujarFondo(c, w, h)
        val hueco = dibujarMarco(ctx, c, w, h)
        dibujarTexto(c, w, h, hueco, palabra)

        val wm = WallpaperManager.getInstance(ctx)
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
        val p = Paint()
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
            Color.rgb(28, 24, 20), Color.rgb(15, 12, 9), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    }

    // Dibuja el marco ajustado por anchura, centrado. Devuelve el rectangulo
    // interior aproximado (el hueco) donde va el texto.
    private fun dibujarMarco(ctx: Context, c: Canvas, w: Int, h: Int): RectF {
        try {
            val marco = BitmapFactory.decodeResource(ctx.resources, R.drawable.marco)
            if (marco != null) {
                // Estirar el marco a pantalla completa
                val dst = RectF(0f, 0f, w.toFloat(), h.toFloat())
                c.drawBitmap(marco, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
                // El hueco interior del marco ronda el 15% lateral y 9% vertical
                val mx = w * 0.15f
                val my = h * 0.09f
                return RectF(mx, my, w - mx, h - my)
            }
        } catch (_: Exception) {}
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.color = Color.rgb(198, 166, 100)
        p.strokeWidth = w * 0.02f
        val m = w * 0.06f
        c.drawRect(m, m, w - m, h - m, p)
        return RectF(m * 2, h * 0.3f, w - m * 2, h * 0.7f)
    }

    private fun dibujarTexto(c: Canvas, w: Int, h: Int, hueco: RectF, palabra: Palabra) {
        val oro = Color.rgb(226, 202, 142)
        val cx = w / 2f
        // Anclado dentro del hueco del marco, algo por debajo del centro
        val cyTermino = hueco.top + hueco.height() * 0.60f

        val pT = Paint(Paint.ANTI_ALIAS_FLAG)
        pT.color = oro
        pT.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        pT.textAlign = Paint.Align.CENTER
        // ancho maximo: el del hueco, con algo de holgura
        val anchoMax = hueco.width() * 0.92f
        pT.textSize = ajustarTamano(pT, palabra.termino, anchoMax, w * 0.11f)
        c.drawText(palabra.termino, cx, cyTermino, pT)

        val pL = Paint(Paint.ANTI_ALIAS_FLAG)
        pL.color = Color.argb(150, 198, 166, 100)
        pL.strokeWidth = 3f
        val lw = w * 0.20f
        val yLinea = cyTermino + h * 0.028f
        c.drawLine(cx - lw, yLinea, cx + lw, yLinea, pL)

        val pD = Paint(Paint.ANTI_ALIAS_FLAG)
        pD.color = Color.rgb(214, 202, 182)
        pD.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        pD.textAlign = Paint.Align.CENTER
        pD.textSize = w * 0.044f

        val lineas = envolver(pD, palabra.definicion, anchoMax)
        var y = yLinea + h * 0.045f
        val salto = pD.textSize * 1.35f
        for (linea in lineas) {
            c.drawText(linea, cx, y, pD)
            y += salto
        }
    }

    private fun ajustarTamano(p: Paint, texto: String, anchoMax: Float, inicial: Float): Float {
        var t = inicial
        p.textSize = t
        while (p.measureText(texto) > anchoMax && t > 20f) {
            t -= 2f
            p.textSize = t
        }
        return t
    }

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
