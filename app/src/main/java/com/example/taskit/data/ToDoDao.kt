package com.example.taskit.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.models.ToDoData

@Dao
interface ToDoDao {

    @Query("SELECT * FROM todo_table ORDER BY id ASC")
    fun getAllData(): LiveData<List<ToDoData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(toDoData: ToDoData)

    @Update
    suspend fun updateData(toDoData: ToDoData)

    @Update
    suspend fun updateList(taskList: TaskList)

    @Delete
    suspend fun deleteItem(toDoData: ToDoData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(taskList: TaskList)

    @Query("DELETE FROM todo_table")
    suspend fun deleteAll()


    @Update
    suspend fun updateListItem(taskList: TaskList)

    @Query("SELECT * FROM todo_table WHERE title LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>>

    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortByHighPriority(): LiveData<List<ToDoData>>

    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'L%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'H%' THEN 3 END")
    fun sortByLowPriority(): LiveData<List<ToDoData>>

    @Query("DELETE FROM list_table WHERE taskNoteId = :id")
    suspend fun deleteAllTasks(id: Int)

    @Delete
    suspend fun deleteList(taskList: TaskList)

    @Query("DELETE FROM list_table WHERE Taskid = :id")
    suspend fun deleteSingleListItem(id: Int)


    @Transaction
    @Query("SELECT * FROM todo_table WHERE ToDoNoteId = :id")
    suspend fun getTodoDataWithTaskList(id: Int): List<ToDoDataWithTaskList>

}