package com.example.taskit.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "list_table")
@Parcelize
data class TaskList(
    var name: String,
    var completed: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var Taskid: Int,
    var taskNoteId: Int

) : Parcelable