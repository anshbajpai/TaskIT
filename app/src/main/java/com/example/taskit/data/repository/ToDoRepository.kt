package com.example.taskit.data.repository

import androidx.lifecycle.LiveData
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.ToDoDataWithTaskList
import com.example.taskit.data.ToDoDao
import com.example.taskit.data.models.ToDoData


class ToDoRepository(private val toDoDao: ToDoDao) {

    val getAllData: LiveData<List<ToDoData>> = toDoDao.getAllData()
    val sortByHighPriority: LiveData<List<ToDoData>> = toDoDao.sortByHighPriority()
    val sortByLowPriority: LiveData<List<ToDoData>> = toDoDao.sortByLowPriority()

    suspend fun insertData(toDoData: ToDoData) {
        toDoDao.insertData(toDoData)
    }

    suspend fun updateData(toDoData: ToDoData) {
        toDoDao.updateData(toDoData)
    }

    suspend fun insertList(taskList: TaskList) {
        toDoDao.insertList(taskList)
    }

    suspend fun updateListItem(taskList: TaskList) {
        toDoDao.updateListItem(taskList)
    }


    suspend fun deleteItem(toDoData: ToDoData) {
        toDoDao.deleteItem(toDoData)
    }

    suspend fun deleteList(taskList: TaskList) {
        toDoDao.deleteList(taskList)
    }

    suspend fun updateList(taskList: TaskList){
        toDoDao.updateList(taskList)
    }


    suspend fun deleteAllTasks(id: Int) {
        toDoDao.deleteAllTasks(id)
    }

    suspend fun deleteAll() {
        toDoDao.deleteAll()
    }

    suspend fun deleteSingleListItem(id: Int) {
        toDoDao.deleteSingleListItem(id)
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>> {
        return toDoDao.searchDatabase(searchQuery)
    }

    suspend fun getTodoDataWithTaskList(id: Int): List<ToDoDataWithTaskList> {
        return toDoDao.getTodoDataWithTaskList(id)
    }


}