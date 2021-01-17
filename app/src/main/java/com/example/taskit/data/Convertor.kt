package com.example.taskit.data

import androidx.room.TypeConverter
import com.example.taskit.data.models.Priority

class Convertor {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }


//
//    @TypeConverter
//    fun fromUri(value: String): Uri {
//            return Uri.parse(value)
//    }
//
//    @TypeConverter
//    fun toUri(uri: Uri): String {
//            return uri.toString()
//    }


}