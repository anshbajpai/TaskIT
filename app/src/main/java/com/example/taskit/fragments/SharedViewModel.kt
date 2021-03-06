package com.example.taskit.fragments

import android.app.Application
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.taskit.R
import com.example.taskit.data.models.Priority
import com.example.taskit.data.models.ToDoData
import java.util.*

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    var calendar: Calendar = Calendar.getInstance()
    var layout: String = "Grid"

    val emptyDatabase: MutableLiveData<Boolean> = MutableLiveData(false)

    fun checkIfDatabaseEmpty(toDoData: List<ToDoData>) {
        emptyDatabase.value = toDoData.isEmpty()
    }

    val listener: AdapterView.OnItemSelectedListener = object :
        AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {}
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            when (position) {
                0 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(
                            application,
                            R.color.red
                        )
                    )
                }
                1 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(
                            application,
                            R.color.yellow
                        )
                    )
                }
                2 -> {
                    (parent?.getChildAt(0) as TextView).setTextColor(
                        ContextCompat.getColor(
                            application,
                            R.color.green
                        )
                    )
                }
            }
        }
    }

    fun verifyDataFromUser(
        title: String,
        description: String,
        date: String,
        time: String
    ): Boolean {

        return if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(
                date
            ) || TextUtils.isEmpty(time)
        ) {
            false
        } else !(title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty())

    }

    fun parsePriority(priority: String): Priority {

        return when (priority) {
            "High Priority" -> {
                Priority.HIGH
            }
            "Medium Priority" -> {
                Priority.MEDIUM
            }
            "Low Priority" -> {
                Priority.LOW
            }

            else -> Priority.LOW
        }

    }
}