package ru.hse.multitracker.data.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    /**
     * Converts timestamp to Date
     * @param value timestamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT)
     * @return date represented by this timestamp
     */
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    /**
     * Converts Date to timestamp
     * @param date
     * @return timestamp (the number of milliseconds since January 1, 1970, 00:00:00 GMT)
     * represented by this Date object
     */
    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}