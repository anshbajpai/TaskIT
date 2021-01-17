package com.example.taskit.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskit.data.models.Priority
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "todo_table")
@Parcelize
data class ToDoData(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var title: String,
    var priority: Priority,
    var description: String,
    var date: Long,
    var time: Long,
    var imageUri: String,
    var ToDoNoteId: Int,
) : Parcelable