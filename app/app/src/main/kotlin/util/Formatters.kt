package com.example.appcantina.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import kotlin.math.roundToInt

object Formatters {
    private val moneyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun todayIso(): String = LocalDate.now().toString()

    fun nowIso(): String = LocalDateTime.now().toString()

    fun money(cents: Int): String = moneyFormat.format(cents / 100.0)

    fun dateLabel(isoDate: String): String = runCatching {
        LocalDate.parse(isoDate).format(dateFormatter)
    }.getOrDefault(isoDate)

    fun dateTimeLabel(isoDateTime: String): String = runCatching {
        LocalDateTime.parse(isoDateTime).format(dateTimeFormatter)
    }.getOrDefault(isoDateTime)

    fun parsePriceToCents(raw: String): Int? {
        val normalized = raw
            .trim()
            .replace("R$", "")
            .replace("\u00A0", "") // Remove non-breaking space
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
        val value = normalized.toDoubleOrNull() ?: return null
        return (value * 100).roundToInt()
    }
}
