package com.example.taskit.fragments.list.adapter

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.getSpans
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskit.R
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.viewmodel.ToDoViewModel
import kotlinx.android.synthetic.main.checklist_layout.view.*


class TaskListAdapter(
    private val mainViewModel: ToDoViewModel
) : ListAdapter<TaskList, TaskListAdapter.TasksViewHolder>(
    DiffCallBack()
) {

    class TasksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.checklist_layout, parent, false)
        return TasksViewHolder(
            view
        )
    }


    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {

        holder.itemView.check_box_completed.isChecked = getItem(position).completed
        holder.itemView.text_view_name.text = getItem(position).name

        if(getItem(position).completed) {
            var spannableString2 = SpannableString(holder.itemView.text_view_name.text)
            spannableString2.setSpan(
                StrikethroughSpan(),
                0,
                holder.itemView.text_view_name.text.length,
                0
            )

            holder.itemView.text_view_name.setText(spannableString2)

        }

        Log.d("TAG1", "Inside: ${getItem(position)}")




        holder.itemView.check_box_completed.setOnCheckedChangeListener { checkBox, isChecked ->


            var spannableString1 = SpannableString("")
            if (isChecked) {

                spannableString1 = SpannableString(holder.itemView.text_view_name.text)
                spannableString1.setSpan(
                    StrikethroughSpan(),
                    0,
                    holder.itemView.text_view_name.text.length,
                    0
                )
                holder.itemView.text_view_name.setText(spannableString1)
                mainViewModel.updateListItem(
                    TaskList(
                        getItem(position).name,
                        isChecked,
                        getItem(position).Taskid,
                        getItem(position).taskNoteId
                    )
                )
//                value = isChecked
            } else {


                holder.itemView.text_view_name.setText(SpannableString(holder.itemView.text_view_name.text.toString()))
                val text = holder.itemView.text_view_name.text.toString()
                Log.d("Heyyyy",text)
//                holder.itemView.text_view_name.setText(text)
                    if(currentList.size > 1) {
                        mainViewModel.updateListItem(
                            TaskList(
                                getItem(position).name,
                                isChecked,
                                getItem(position).Taskid,
                                getItem(position).taskNoteId
                            )
                        )
                    }
//                value = isChecked
            }
        }


    }

}