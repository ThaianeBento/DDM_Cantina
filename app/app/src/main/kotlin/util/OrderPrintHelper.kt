package com.example.appcantina.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.appcantina.data.local.OrderWithLines
import com.example.appcantina.ui.components.consumptionLabel
import com.example.appcantina.ui.components.statusLabel
import java.io.FileOutputStream
import kotlin.math.ceil

object OrderPrintHelper {
    private const val pageWidth = 595
    private const val pageHeight = 842
    private const val margin = 40f
    private const val lineHeight = 18f
    private const val maxLinesPerPage = 40

    fun printDailyOrders(
        context: Context,
        orderDateIso: String,
        orders: List<OrderWithLines>
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val lines = buildPrintLines(orderDateIso, orders)
        val jobName = "Pedidos almoco $orderDateIso"
        printManager.print(
            jobName,
            LinesPrintAdapter(jobName, lines),
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build()
        )
    }

    private fun buildPrintLines(orderDateIso: String, orders: List<OrderWithLines>): List<String> {
        val total = orders.sumOf { it.order.totalCents }
        val accepted = orders.count { it.order.status == "CONFIRMED" }
        val pending = orders.count { it.order.status == "PENDING" }
        val rejected = orders.count { it.order.status == "REJECTED" }
        val lines = mutableListOf<String>()

        lines += "Cozinha Bem-Estar"
        lines += "Relacao de pedidos - almoco de ${Formatters.dateLabel(orderDateIso)}"
        lines += "Gerado em ${Formatters.dateTimeLabel(Formatters.nowIso())}"
        lines += "Total de pedidos: ${orders.size} | Aceitos: $accepted | Pendentes: $pending | Recusados: $rejected"
        lines += "Valor total listado: ${Formatters.money(total)}"
        lines += ""

        if (orders.isEmpty()) {
            lines += "Nenhum pedido registrado para esta data."
            return lines
        }

        orders.forEach { orderWithLines ->
            val order = orderWithLines.order
            lines += "Pedido #${order.id} | ${order.userEmail}"
            lines += "Status: ${statusLabel(order.status)} | Consumo: ${consumptionLabel(order.consumptionType)} | Total: ${Formatters.money(order.totalCents)}"
            orderWithLines.lines.forEach { line ->
                lines += "  ${line.quantity}x ${line.itemName} - ${Formatters.money(line.quantity * line.unitPriceCents)}"
            }
            lines += ""
        }

        return lines.flatMap { wrapLine(it) }
    }

    private fun wrapLine(line: String): List<String> {
        if (line.length <= 82) return listOf(line)

        val wrapped = mutableListOf<String>()
        var remaining = line
        while (remaining.length > 82) {
            val splitIndex = remaining.take(82).lastIndexOf(' ').takeIf { it > 0 } ?: 82
            wrapped += remaining.take(splitIndex)
            remaining = remaining.drop(splitIndex).trimStart()
        }
        if (remaining.isNotEmpty()) wrapped += remaining
        return wrapped
    }

    private class LinesPrintAdapter(
        private val documentName: String,
        private val lines: List<String>
    ) : PrintDocumentAdapter() {
        private val pageCount = ceil(lines.size / maxLinesPerPage.toDouble()).toInt().coerceAtLeast(1)

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: android.os.Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            val info = PrintDocumentInfo.Builder("$documentName.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pageCount)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            val document = PdfDocument()
            val paint = Paint().apply {
                textSize = 12f
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            val titlePaint = Paint(paint).apply {
                textSize = 16f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }

            try {
                repeat(pageCount) { pageIndex ->
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        return
                    }

                    val pageNumber = pageIndex + 1
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    val page = document.startPage(pageInfo)
                    val canvas = page.canvas
                    var y = margin

                    canvas.drawText("Cozinha Bem-Estar", margin, y, titlePaint)
                    y += lineHeight * 1.5f

                    val start = pageIndex * maxLinesPerPage
                    val end = minOf(start + maxLinesPerPage, lines.size)
                    lines.subList(start, end).forEach { line ->
                        canvas.drawText(line, margin, y, paint)
                        y += lineHeight
                    }

                    canvas.drawText("Pagina $pageNumber de $pageCount", margin, pageHeight - margin, paint)
                    document.finishPage(page)
                }

                FileOutputStream(destination.fileDescriptor).use { output ->
                    document.writeTo(output)
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (exception: Exception) {
                callback.onWriteFailed(exception.message)
            } finally {
                document.close()
            }
        }
    }
}
