package com.example.crashcourse.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromFloatArray(array: FloatArray): String =
        array.joinToString(",")

    @TypeConverter
    fun toFloatArray(data: String): FloatArray =
        data.split(",").map { it.toFloat() }.toFloatArray()

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): Long =
        dateTime.toEpochSecond(ZoneOffset.UTC)

    @TypeConverter
    fun toLocalDateTime(timestamp: Long): LocalDateTime =
        LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
}
