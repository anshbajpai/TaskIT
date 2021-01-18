package com.example.taskit.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

const val PREFERENCES_NAME = "my_preferences"

class DataStoreRepository(context: Context) {

    private object PreferenceKeys {
        val name = preferencesKey<String>("my_layout")
        val number = preferencesKey<Int>("my_autoNum")
        val firstLaunch = preferencesKey<Boolean>("my_firstLaunch")
    }


    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = PREFERENCES_NAME
    )

    suspend fun saveToDataStore(name: String) {
        dataStore.edit {
            it[PreferenceKeys.name] = name
        }
    }

    suspend fun saveBolToDataStore(firstLaunch:Boolean){
        dataStore.edit {
            it[PreferenceKeys.firstLaunch] = firstLaunch
        }
    }

    suspend fun saveIntToDataStore(number: Int) {
        dataStore.edit {
            it[PreferenceKeys.number] = number
        }
    }

    val readIntDataStore: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStore", exception.message.toString())
            } else {
                throw exception
            }
        }
        .map {
            val myNumber = it[PreferenceKeys.number] ?: 0
            myNumber
        }

    val readBolDataStore: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStore", exception.message.toString())
            } else {
                throw exception
            }
        }
        .map {
            val isFirstLaunch = it[PreferenceKeys.firstLaunch] ?: true
            isFirstLaunch
        }

    val readFromDataStore: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStore", exception.message.toString())
            } else {
                throw exception
            }
        }
        .map {
            val myLayout = it[PreferenceKeys.name] ?: "Grid"
            myLayout
        }
}