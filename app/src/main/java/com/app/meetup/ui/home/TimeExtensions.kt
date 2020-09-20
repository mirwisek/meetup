package com.app.meetup.ui.home

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import org.threeten.bp.*

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

fun LatLng.toGeoPoint(): GeoPoint {
    return GeoPoint(latitude, longitude)
}

fun LocalDateTime.combineFormat(endTime: LocalDateTime): String {
    return "From ${toLocalDate().getFormatted()} ${toLocalTime().getFormatted()} to " +
            "${endTime.toLocalDate().getFormatted()} ${endTime.toLocalTime().getFormatted()}"
}