package com.example.taskit.fragments.list.adapter

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.taskit.data.models.TaskList


class DiffCallBack(
) : DiffUtil.ItemCallback<TaskList>() {
    override fun areItemsTheSame(oldItem: TaskList, newItem: TaskList): Boolean {

        Log.d("TAG3", " Items: ${oldItem.Taskid == newItem.Taskid}")
        return oldItem.Taskid == newItem.Taskid

    }

    override fun areContentsTheSame(oldItem: TaskList, newItem: TaskList): Boolean {

        Log.d("TAG3", " Contents: ${oldItem == newItem}")
        return oldItem == newItem

    }


}