package com.example.appcantina.util

import com.example.appcantina.data.local.FormConfigEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class OrderWindow(
    val orderDate: LocalDate,
    val isOpen: Boolean,
    val opensAt: LocalDateTime,
    val closesAt: LocalDateTime,
    val openTime: LocalTime,
    val closeTime: LocalTime
) {
    val orderDateIso: String = orderDate.toString()
    val opensAtIso: String = opensAt.toString()
    val closesAtIso: String = closesAt.toString()
    val openTimeLabel: String = openTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    val closeTimeLabel: String = closeTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

object OrderRules {
    private val defaultOpenTime = LocalTime.of(19, 0)
    private val defaultCloseTime = LocalTime.of(8, 0)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun currentWindow(
        config: FormConfigEntity? = null,
        now: LocalDateTime = LocalDateTime.now()
    ): OrderWindow {
        val openTime = parseTime(config?.orderOpenTime) ?: defaultOpenTime
        val closeTime = parseTime(config?.orderCloseTime) ?: defaultCloseTime
        val today = now.toLocalDate()
        val currentTime = now.toLocalTime()
        val crossesMidnight = !closeTime.isAfter(openTime)

        return if (crossesMidnight) {
            overnightWindow(today, currentTime, openTime, closeTime)
        } else {
            sameDayWindow(today, currentTime, openTime, closeTime)
        }
    }

    fun normalizeTime(raw: String): String? = parseTime(raw)?.format(timeFormatter)

    private fun parseTime(raw: String?): LocalTime? {
        val normalized = raw
            ?.trim()
            ?.replace("h", ":", ignoreCase = true)
            ?.let { if (it.matches(Regex("^\\d{1,2}$"))) "$it:00" else it }
            ?: return null

        return runCatching { LocalTime.parse(normalized, timeFormatter) }.getOrNull()
    }

    private fun overnightWindow(
        today: LocalDate,
        currentTime: LocalTime,
        openTime: LocalTime,
        closeTime: LocalTime
    ): OrderWindow {
        return when {
            !currentTime.isBefore(openTime) -> {
                val closesAt = LocalDateTime.of(today.plusDays(1), closeTime)
                buildWindow(
                    isOpen = true,
                    opensAt = LocalDateTime.of(today, openTime),
                    closesAt = closesAt,
                    openTime = openTime,
                    closeTime = closeTime
                )
            }

            currentTime.isBefore(closeTime) -> {
                val closesAt = LocalDateTime.of(today, closeTime)
                buildWindow(
                    isOpen = true,
                    opensAt = LocalDateTime.of(today.minusDays(1), openTime),
                    closesAt = closesAt,
                    openTime = openTime,
                    closeTime = closeTime
                )
            }

            else -> {
                val closesAt = LocalDateTime.of(today.plusDays(1), closeTime)
                buildWindow(
                    isOpen = false,
                    opensAt = LocalDateTime.of(today, openTime),
                    closesAt = closesAt,
                    openTime = openTime,
                    closeTime = closeTime
                )
            }
        }
    }

    private fun sameDayWindow(
        today: LocalDate,
        currentTime: LocalTime,
        openTime: LocalTime,
        closeTime: LocalTime
    ): OrderWindow {
        val isCurrentlyOpen = !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime)
        val openDate = if (currentTime.isBefore(openTime) || isCurrentlyOpen) today else today.plusDays(1)
        val closesAt = LocalDateTime.of(openDate, closeTime)

        return buildWindow(
            isOpen = isCurrentlyOpen,
            opensAt = LocalDateTime.of(openDate, openTime),
            closesAt = closesAt,
            openTime = openTime,
            closeTime = closeTime
        )
    }

    private fun buildWindow(
        isOpen: Boolean,
        opensAt: LocalDateTime,
        closesAt: LocalDateTime,
        openTime: LocalTime,
        closeTime: LocalTime
    ): OrderWindow {
        return OrderWindow(
            orderDate = closesAt.toLocalDate(),
            isOpen = isOpen,
            opensAt = opensAt,
            closesAt = closesAt,
            openTime = openTime,
            closeTime = closeTime
        )
    }
}
