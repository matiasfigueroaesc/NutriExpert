package cl.duoc.nutriexpert.ui.plan

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import cl.duoc.nutriexpert.data.local.PlanEntity

fun exportarPlanPdf(context: Context, uri: Uri, plan: PlanEntity) {
    val documento = PdfDocument()
    val pintura = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val ancho = 595
    val alto = 842
    val margen = 45f
    val anchoTexto = ancho - margen * 2
    val interlineado = 17f
    var numeroPagina = 0
    var pagina: PdfDocument.Page? = null
    var y = margen

    fun abrirPagina() {
        numeroPagina++
        pagina = documento.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numeroPagina).create())
        y = margen
    }

    fun cerrarPagina() {
        pagina?.let { documento.finishPage(it) }
        pagina = null
    }

    fun escribirLinea(texto: String) {
        if (pagina == null) abrirPagina()
        if (y + interlineado > alto - margen) { cerrarPagina(); abrirPagina() }
        pagina?.canvas?.drawText(texto, margen, y, pintura)
        y += interlineado
    }

    try {
        textoDetallePlan(plan).lines().forEach { linea ->
            if (linea.isBlank()) {
                escribirLinea("")
            } else {
                val palabras = linea.split(" ")
                var actual = ""
                palabras.forEach { palabra ->
                    val candidato = if (actual.isEmpty()) palabra else "$actual $palabra"
                    if (pintura.measureText(candidato) > anchoTexto && actual.isNotEmpty()) {
                        escribirLinea(actual)
                        actual = palabra
                    } else {
                        actual = candidato
                    }
                }
                if (actual.isNotEmpty()) escribirLinea(actual)
            }
        }
        cerrarPagina()
        context.contentResolver.openOutputStream(uri)?.use { salida ->
            documento.writeTo(salida)
        } ?: error("No se pudo abrir el archivo PDF")
    } finally {
        documento.close()
    }
}
