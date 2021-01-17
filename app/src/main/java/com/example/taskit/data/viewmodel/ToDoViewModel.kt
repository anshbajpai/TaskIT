package com.example.taskit.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskit.data.repository.DataStoreRepository
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.ToDoDatabase
import com.example.taskit.data.models.ToDoData
import com.example.taskit.data.ToDoDataWithTaskList
import com.example.taskit.data.repository.ToDoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToDoViewModel(application: Application) : AndroidViewModel(application) {

    private val toDoDao = ToDoDatabase.getDatabase(application).todoDao()
    private val repository: ToDoRepository

    private val dataStoreRepository =
        DataStoreRepository(application)

    val readFromDataStore = dataStoreRepository.readFromDataStore.asLiveData()

    val readIntDataStore = dataStoreRepository.readIntDataStore.asLiveData()


    val sortByHighPriority: LiveData<List<ToDoData>>
    val sortByLowPriority: LiveData<List<ToDoData>>


    val getAllData: LiveData<List<ToDoData>>

    init {
        repository = ToDoRepository(toDoDao)
        getAllData = repository.getAllData
        sortByHighPriority = repository.sortByHighPriority
        sortByLowPriority = repository.sortByLowPriority
    }

    fun saveToDataStore(myLayout: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveToDataStore(myLayout)
        }


    }

    fun saveIntDataStore(number: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveIntToDataStore(number)
        }


    }

    fun insertData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(toDoData)
        }
    }

    fun insertList(taskList: TaskList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertList(taskList)
        }
    }

    fun updateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateData(toDoData)
        }
    }

    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(toDoData)
        }
    }

    fun deleteList(taskList: TaskList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteList(taskList)
        }
    }

    fun updateListItem(taskList: TaskList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateListItem(taskList)
        }
    }

    fun updateList(taskList: TaskList){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateList(taskList)
        }
    }

    fun deleteSingleListItem(id:Int){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSingleListItem(id)
        }
    }

    fun deleteAllTasks(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTasks(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<ToDoData>> {
        return repository.searchDatabase(searchQuery)
    }

    suspend fun getTodoDataWithTaskList(id: Int): List<ToDoDataWithTaskList> {

          return  repository.getTodoDataWithTaskList(id)
    }

}