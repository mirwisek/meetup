package com.app.meetup.ui.home

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

private val offset = OffsetDateTime.now().offset

fun LocalDate.getFormatted(): String {
    return "$dayOfMonth/$monthValue/$year"
}

fun LocalTime.getFormatted(): String {
    var p = "AM"
    var finalHour = hour
    if (hour > 12) {
        finalHour = hour - 12
        p = "PM"
    }
    return "$finalHour:$minute $p"
}

fun LocalDateTime.getFormatted(): String {
    return "${toLocalDate().getFormatted()}\n${toLocalTime().getFormatted()}"
}

fun LocalDateTime.toTimestamp(): Timestamp {
    return Timestamp(toEpochSecond(offset), nano)
}

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(seconds, nanoseconds, offset)
}

fun Timestamp.toLocalTime(): LocalTime {
    return toLocalDateTime().toLocalTime()
}

fun LatLng.toGeoPoint(): GeoPoint {
    return GeoPoint(latitude, longitude)
}

fun LocalDateTime.combineFormat(endTime: LocalDateTime): String {
    val startFormat = DateTimeFormatter.ofPattern("E, MMM d HH:mm a")
    return "${format(startFormat)} - " + endTime.toLocalTime().getFormatted()
}

fun LocalTime.combineTimeFormat(endTime: LocalTime): String {
    val t1 = formatTime
    val t2 = endTime.formatTime
    val p = t1.substringAfter(" ")
    return if(t2.endsWith(p))
        formatTime.substringBeforeLast(" ") + " - " + t2
    else
        "$formatTime - $t2"
}

val LocalDate.formatDate: String
get() {
    val startFormat = DateTimeFormatter.ofPattern("E, MMM d")
    return format(startFormat)
}

val LocalTime.formatTime: String
    get() {
        val startFormat = DateTimeFormatter.ofPattern("HH:mm a")
        return format(startFormat)
    }