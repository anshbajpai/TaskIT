package com.example.taskit.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.models.ToDoData

data class ToDoDataWithTaskList(
    @Embedded val todoData: ToDoData,
    @Relation(
        parentColumn = "ToDoNoteId",
        entityColumn = "taskNoteId"
    )
    val taskList: List<TaskList>
)