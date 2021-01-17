package com.example.taskit.fragments.list.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskit.R
import com.example.taskit.data.ToDoDataWithTaskList
import com.example.taskit.data.models.Priority
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.models.ToDoData
import com.example.taskit.data.viewmodel.ToDoViewModel
import com.example.taskit.fragments.list.listFragmentDirections
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.row_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class ListAdapter(
    private val requireActivity: FragmentActivity,
    private val mainViewModel: ToDoViewModel,
) : RecyclerView.Adapter<ListAdapter.MyViewHolder>(), ActionMode.Callback {


    var dataList = emptyList<ToDoData>()


    private var selectedNotes = arrayListOf<ToDoData>()
    private var selectedLists = arrayListOf<TaskList>()
    private var myViewHolders = arrayListOf<MyViewHolder>()
    private var multiSelection = false

    private lateinit var mActionMode: ActionMode
    private lateinit var mShareItem: MenuItem
    private lateinit var rootView: View

    var term = true

    //private val madapter: TaskListAdapter by lazy { TaskListAdapter() }

    var adapterList: MutableList<TaskListAdapter> = arrayListOf()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        myViewHolders.add(holder)
        rootView = holder.itemView.rootView
        holder.itemView.title_txt.text = dataList[position].title
        holder.itemView.description_txt.text = dataList[position].description


        var it: List<ToDoDataWithTaskList> = emptyList()
        val madapter = TaskListAdapter(
            mainViewModel
        )


        CoroutineScope(Dispatchers.IO).launch{
            it =  mainViewModel.getTodoDataWithTaskList(dataList[position].ToDoNoteId)

            if (it.isNotEmpty()) {
                if (it[0].taskList.isNotEmpty()) {




                        CoroutineScope(Dispatchers.Main).launch {
                            holder.itemView.check_main_recycler_view.adapter = madapter
                            holder.itemView.check_main_recycler_view.layoutManager =
                                LinearLayoutManager(requireActivity)
                            holder.itemView.check_main_recycler_view.isVisible = true
                            holder.itemView.description_txt.isVisible = false
                        }

                    val checkList: MutableList<TaskList> = arrayListOf()
                    it[0].taskList.forEach {


                        val adapterList = checkList + it
                        checkList.add(it)
                        madapter.submitList(adapterList)

                    }
                }else{
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.itemView.check_main_recycler_view.isVisible = false
                        holder.itemView.description_txt.isVisible = true
                    }

                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                         holder.itemView.check_main_recycler_view.isVisible = false
                      holder.itemView.description_txt.isVisible = true
                }


            }

        }

        if (dataList[position].imageUri != "") {
            holder.itemView.main_imageNote.setImageURI(Uri.parse(dataList[position].imageUri))
            holder.itemView.main_imageNote.isVisible = true
            Log.d("Inside", "Inside2")
        }
        else {
            holder.itemView.main_imageNote.setImageURI(Uri.EMPTY)
            holder.itemView.main_imageNote.isVisible = false
        }

        val priority = dataList[position].priority
        when (priority) {
            Priority.HIGH -> holder.itemView.priority_indicator.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.red
                )
            )
            Priority.LOW -> holder.itemView.priority_indicator.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.green
                )
            )
            Priority.MEDIUM -> holder.itemView.priority_indicator.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.yellow
                )
            )
        }

        holder.itemView.row_background.setOnClickListener {
            Log.d("Inside", "Inside3")
            if (multiSelection) {
                if(madapter.currentList.size == 0) {
                    applySelection(holder, dataList[position])
                }
                else {
                    applyListSelection(holder,dataList[position] , madapter.currentList)
                }
            } else {
//                var item = arrayOf<TaskList>()
//                item.copyOf()
//                if(checkList.size !=0) {
//                    checkList.forEach {
//                            for (i in 0..checkList.size) {
//                                Log.d("Taggz",it.toString())
//                                item[i] = it
//                            }
//
//                    }
//                }
                val action =
                    listFragmentDirections.actionListFragmentToUpdateFragment(dataList[position])
                clearContextualActionMode()
                holder.itemView.findNavController().navigate(action)
            }
        }


        holder.itemView.row_background.setOnLongClickListener {
            Log.d("Inside", "Inside4")
            if (!multiSelection) {
                multiSelection = true
                requireActivity.startActionMode(this)
                Log.d("CurrentList: ",madapter.currentList.toString())
                if(madapter.currentList.size == 0) {
                    Log.d("Current: ", "Inside Note")
                    applySelection(holder, dataList[position])
                }
                else {
                    Log.d("Current: ", "Inside List")
                    applyListSelection(holder,dataList[position] , madapter.currentList)
                }
                true
            } else {
                multiSelection = false
                false
            }

        }
    }

    private fun applyListSelection(holder: ListAdapter.MyViewHolder, currentNote: ToDoData, currentList: List<TaskList>) {

        if(selectedNotes.contains(currentNote)) {
            selectedNotes.remove(currentNote)
            currentList.forEach {
                selectedLists.remove(it)
            }
            changeNoteStyle(holder, R.color.white, R.color.lightGray)
            applyActionModeTitle()
        }
        else {
            selectedNotes.add(currentNote)
            currentList.forEach {
                selectedLists.add(it)
            }
            changeNoteStyle(holder, R.color.cardBackground, R.color.colorPrimary)
            applyActionModeTitle()
        }

    }

    private fun applySelection(holder: MyViewHolder, currentNote: ToDoData) {

        if (selectedNotes.contains(currentNote)) {
            selectedNotes.remove(currentNote)
            selectedLists.clear()
            changeNoteStyle(holder, R.color.white, R.color.lightGray)
            applyActionModeTitle()
        } else {
            selectedNotes.add(currentNote)
            selectedLists.clear()
            changeNoteStyle(holder, R.color.cardBackground, R.color.colorPrimary)
            applyActionModeTitle()
        }

    }


    private fun changeNoteStyle(holder: MyViewHolder, backgroundColor: Int, strokeColor: Int) {
        holder.itemView.row_background.setBackgroundColor(
            ContextCompat.getColor(requireActivity, backgroundColor)
        )

        holder.itemView.favorite_row_cardView.setStrokeColor(
            ContextCompat.getColor(requireActivity, strokeColor)
        )
    }

    private fun applyActionModeTitle() {
        when (selectedNotes.size) {
            0 -> {
                mActionMode.finish()
            }
            1 -> {
                mShareItem.isVisible = true
                mActionMode.title = "${selectedNotes.size} item selected"
            }
            else -> {
                mShareItem.isVisible = false
                mActionMode.title = "${selectedNotes.size} items selected"
            }
        }
    }


    fun setData(todoData: List<ToDoData>) {
        val toDoDiffUtil = ToDoDiffUtil(dataList, todoData)
        val toDoDiffResult = DiffUtil.calculateDiff(toDoDiffUtil)
        this.dataList = todoData
        toDoDiffResult.dispatchUpdatesTo(this)
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {

        if (item?.itemId == R.id.delete_item) {
            selectedNotes.forEach {
                mainViewModel.deleteItem(it)
            }
            selectedLists.forEach {
                mainViewModel.deleteList(it)
            }
            showSnackBar("${selectedNotes.size} Task/s removed.")

            multiSelection = false
            selectedNotes.clear()
            selectedLists.clear()
            mode?.finish()
        } else if (item?.itemId == R.id.share_item) {
            if(selectedLists.size == 0) {
                    Log.d("Size: ", selectedLists.size.toString())
                selectedNotes.forEach {
                    val sharingIntent = Intent(Intent.ACTION_SEND)

                    sharingIntent.setType("text/plain")

                    val shareBody = "Title: ${it.title} \nDescription: ${it.description}"

                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                    requireActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"))

                }
            }
            else {
                Log.d("Size: ", selectedLists.size.toString())
                var shareBodyOut = ""
                var sharingIntentOut:Intent = Intent()
                selectedNotes.forEach {
                    shareBodyOut = "Title: ${it.title} \n"
                }

                selectedLists.forEach {
                    sharingIntentOut = Intent(Intent.ACTION_SEND)

                    sharingIntentOut.setType("text/plain")

                    shareBodyOut +=  "# ${it.name}\n"
                }

                sharingIntentOut.putExtra(Intent.EXTRA_TEXT, shareBodyOut);

                requireActivity.startActivity(Intent.createChooser(sharingIntentOut, "Share via"))


            }
        }
        return true

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            rootView,
            message,
            Snackbar.LENGTH_SHORT
        ).setAction("Okay") {}
            .show()
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.recycler_view_menu, menu)
        mShareItem = mode!!.getMenu().findItem(R.id.share_item);
        mActionMode = mode!!
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {

        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

        myViewHolders.forEach { holder ->
            changeNoteStyle(holder, R.color.white, R.color.lightGray)
        }

        multiSelection = false
        selectedNotes.clear()
    }

    fun clearContextualActionMode() {
        if (this::mActionMode.isInitialized) {
            mActionMode.finish()
        }
    }

}