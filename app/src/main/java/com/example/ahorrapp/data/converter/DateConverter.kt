package com.example.ahorrapp.data.converter

import androidx.room.TypeConverter
import com.example.ahorrapp.data.model.RepeatInterval
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromRepeatInterval(value: RepeatInterval?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toRepeatInterval(value: String?): RepeatInterval? {
        return value?.let { RepeatInterval.valueOf(it) }
    }
} 