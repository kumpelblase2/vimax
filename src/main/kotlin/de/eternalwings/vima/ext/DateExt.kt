package de.eternalwings.vima.ext

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Date.toLocalTime(): LocalTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
}
