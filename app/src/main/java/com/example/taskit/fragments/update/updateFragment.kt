package com.example.taskit.fragments.update

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.taskit.R
import com.example.taskit.SwipeToDeleteCallback
import com.example.taskit.data.ToDoDataWithTaskList
import com.example.taskit.data.models.Priority
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.models.ToDoData
import com.example.taskit.data.viewmodel.ToDoViewModel
import com.example.taskit.fragments.SharedViewModel
import com.example.taskit.fragments.add.addFragment
import com.example.taskit.fragments.list.SwipeToDelete
import com.example.taskit.fragments.list.adapter.TaskListAdapter
import com.example.taskit.notifications.NotifyWork
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.maxkeppeler.bottomsheets.input.InputSheet
import com.maxkeppeler.bottomsheets.input.type.InputSpinner
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.checklist_layout.view.*
import kotlinx.android.synthetic.main.custom_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class updateFragment : Fragment() {


    lateinit var currentTimeSetListener: TimePickerDialog.OnTimeSetListener
    lateinit var currentDateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var imageUri: Uri
    var currentFinalDate = 0L
    var currentFinalTime = 0L
    lateinit var pickedPriority: String

    var checkList: MutableList<TaskList> = ArrayList()
    private var j = 0


    private var itemList: MutableList<TaskList> = arrayListOf()

    private val args by navArgs<updateFragmentArgs>()

    private var layoutListItem: String = "Note"

    private val mShareViewModel: SharedViewModel by viewModels()
    private val mTodoViewModel: ToDoViewModel by viewModels()

    private val madapter: TaskListAdapter by lazy {
        TaskListAdapter(
            mTodoViewModel
        )
    }

    private var compareList: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)

        val myformatTime = "h:mm a"
        val sdfTime = SimpleDateFormat(myformatTime)
        val myformatDate = "dd/MM/yy"
        val sdfDate = SimpleDateFormat(myformatDate)
        val priorityIndex = parsePriority(args.currentItem.priority)


        view.current_titleEt.setText(args.currentItem.title)
        view.current_descEt.setText(args.currentItem.description)
        view.current_spinnerCategory.setSelection(parsePriority(args.currentItem.priority))
        currentFinalTime = args.currentItem.time
        currentFinalDate = args.currentItem.date


        val recyclerViewNew = view.current_checkbox_recyclerView
        recyclerViewNew.adapter = madapter
        recyclerViewNew.layoutManager = LinearLayoutManager(requireContext())

        var resultAns: ToDoDataWithTaskList


        swipeToDelete(recyclerViewNew)


        CoroutineScope(Dispatchers.IO).launch {
            var it = mTodoViewModel.getTodoDataWithTaskList(args.currentItem.ToDoNoteId)
            resultAns = it[0]

            CoroutineScope(Dispatchers.Main).launch {
                if (resultAns.taskList.isNotEmpty()) {

                    view.current_checkbox_recyclerView.isVisible = true
                    view.current_checkbox_edit_text.isVisible = true
                    view.current_descEt.isVisible = false

                    resultAns.taskList.forEach {
                        checkList.add(it)
                    }
                    madapter.submitList(resultAns.taskList)
                    compareList = checkList.size
                    layoutListItem = "Check"
                    requireActivity().current_bottom_nav?.selectedItemId = R.id.check_box_item
                } else {
                    layoutListItem = "Note"
                    requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
                }

            }
        }


//        mTodoViewModel.getTodoDataWithTaskList(args.currentItem.ToDoNoteId)
//            .observe(viewLifecycleOwner, {
//                resultAns = it[0]
//
//                if (resultAns.taskList.isNotEmpty()) {
//                    view.current_checkbox_recyclerView.isVisible = true
//                    view.current_checkbox_edit_text.isVisible = true
//                    view.current_descEt.isVisible = false
//                    resultAns.taskList.forEach {
//                        checkList.add(it)
//                    }
//                    madapter.submitList(resultAns.taskList)
//                    compareList = checkList.size
//                    layoutListItem = "Check"
//                    requireActivity().current_bottom_nav?.selectedItemId = R.id.check_box_item
//                } else {
//                    layoutListItem = "Note"
//                    requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
//                }
//            })

        pickedPriority = when (priorityIndex) {
            0 -> "High Priority"
            1 -> "Medium Priority"
            2 -> "Low Priority"
            else -> "Low Priority"

        }

//        view.current_timeEdt.setText(sdfTime.format(args.currentItem.time))
//        view.current_dateEdit.setText(sdfDate.format(args.currentItem.date))
        view.current_spinnerCategory.onItemSelectedListener = mShareViewModel.listener
        view.current_imageNote.setImageURI(Uri.parse(args.currentItem.imageUri))


        val df = SimpleDateFormat("dd-MMM-yyyy")
        val date = df.format(Calendar.getInstance().time)
        view.current_date_view.text = date




        view.current_delete_btn.setOnClickListener {





                confirmItemRemoval()


//                currentDateSetListener =
//                    DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
//
//
//                        mShareViewModel.calendar.set(Calendar.YEAR, year)
//                        mShareViewModel.calendar.set(Calendar.MONTH, month)
//                        mShareViewModel.calendar.set(Calendar.DAY_OF_MONTH, day)
//
//                        val myformat = "dd/MM/yy"
//
//                        val sdf = SimpleDateFormat(myformat)
//                        dialogView.dateEdit.setText((sdf.format(mShareViewModel.calendar.time)))
//
//
//                    }
//                val datePickerDialog = DatePickerDialog(
//                    requireActivity(),
//                    currentDateSetListener,
//                    mShareViewModel.calendar.get(Calendar.YEAR),
//                    mShareViewModel.calendar.get(Calendar.MONTH),
//                    mShareViewModel.calendar.get(Calendar.DAY_OF_MONTH)
//                )
//
//                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
//                datePickerDialog.show()
//
//            }
//
//
////
//////
////            // For Time Picker
////            updateTime()
//
//            dialogView.timeEdt.setOnClickListener {
//
//
//                // Setting a time listener , by building the Time Picker Dialog
//                currentTimeSetListener =
//                    TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, min: Int ->
//
//                        // Converting the time picker to appropriate format needed in which is hours and mins
//                        mShareViewModel.calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
//                        mShareViewModel.calendar.set(Calendar.MINUTE, min)
//
//
//                        val myformat = "h:mm a"
//
//                        val sdf = SimpleDateFormat(myformat)
//                        // Finally Displaying The Text
//                        dialogView.timeEdt.setText((sdf.format(mShareViewModel.calendar.time)))
//
//
//                    }
//                // Creating Instance Of Time Picker Dialog
//                val timePickerDialog = TimePickerDialog(
//                    activity,
//                    currentTimeSetListener,
//                    mShareViewModel.calendar.get(Calendar.HOUR_OF_DAY),
//                    mShareViewModel.calendar.get(Calendar.MINUTE),
//                    false
//                )
//                // Displaying The Dialog
//                timePickerDialog.show()






//
//            dialogView.okBtn.setOnClickListener {
//
//                if (!(dialogView.dateEdit.text.isNullOrEmpty() || dialogView.timeEdt.text.isNullOrEmpty())) {
//                    currentFinalTime = mShareViewModel.calendar.time.time
//                    currentFinalDate = mShareViewModel.calendar.time.time
//
//                    dialogBuilder.dismiss()
//                } else {
//                    Toast.makeText(
//                        requireActivity(),
//                        "Fields can't be left empty",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//
//            }
//            dialogView.cancelBtn.setOnClickListener {
//                dialogBuilder.dismiss()
//            }
//
//
//            dialogBuilder.apply {
//                window?.setBackgroundDrawableResource(android.R.color.transparent)
//                setView(dialogView)
//                setCancelable(true)
//                show()
//            }

        }




        view.current_bottom_nav.setOnNavigationItemSelectedListener {

            if (it.itemId == R.id.priority_item) {
                InputSheet().show(requireActivity()) {
                    title("Choose Priority")
                    content("Here you can prioritize your task on it's importance !")
                    cancelableOutside(false)

                    var indexSpinner = -1


                    with(InputSpinner() {
                        required()

                        label("Pick Priortity")


                        options(mutableListOf("High Priority", "Medium Priority", "Low Priority" , "Low Priority"))
                        selected(parsePriority(args.currentItem.priority))

                        changeListener { it ->
                            indexSpinner = it
                        }

                    })
                    onPositive {

                        pickedPriority = when (indexSpinner) {
                            0 -> {
                                "High Priority"
                            }
                            1 -> {
                                "Medium Priority"
                            }
                            2 -> {
                                "Low Priority"
                            }
                            else -> {
                                "Low Priority"
                            }
                        }

                        Toast.makeText(requireContext(), pickedPriority, Toast.LENGTH_SHORT).show()

                        requireActivity().current_bottom_nav?.selectedItemId = R.id.check_box_item


                    }


                    onNegative {
                        requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
                    }
                }

            } else if (it.itemId == R.id.gallery_item) {

                if(layoutListItem == "Check"){
                    Toast.makeText(requireContext(),"Images are only supported in Note/Description Mode",Toast.LENGTH_SHORT).show()

                    Handler().postDelayed( { requireActivity().current_bottom_nav?.selectedItemId = R.id.check_box_item }, 500)
                }
                else {

                    checkPermissionForImage()
                    requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
                }


            } else if (it.itemId == R.id.camera_item) {

                if(layoutListItem == "Check"){
                    Toast.makeText(requireContext(),"Images are only supported in Note/Description Mode",Toast.LENGTH_SHORT).show()


                    Handler().postDelayed( { requireActivity().current_bottom_nav?.selectedItemId = R.id.check_box_item }, 500)
                }
                else {

                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            1003
                        );
                    } else {
                        var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 1888);
                    }

                }


            } else if (it.itemId == R.id.check_box_item) {
                if (layoutListItem == "Note") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are You Sure ?")
                        .setMessage("Do you want to shift to CheckList mode ? Your note will be discarded!")
                        .setNegativeButton("Cancel") { _, _ ->
                            // Respond to negative button press
                        }
                        .setPositiveButton("Ok") { _, _ ->

                            current_descEt.isVisible = false
                            current_checkbox_recyclerView.isVisible = true
                            current_checkbox_edit_text.isVisible = true
                            current_descEt.setText("")
                            layoutListItem = "Check"

                        }
                        .show()
                }
            } else if (it.itemId == R.id.note_item) {
                if (layoutListItem == "Check") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are You Sure ?")
                        .setMessage("Do you want to shift to Note mode ? Your CheckList will be discarded!")
                        .setNegativeButton("Cancel") { _, _ ->
                            // Respond to negative button press
                        }
                        .setPositiveButton("Ok") { _, _ ->

                            current_descEt.isVisible = true
                            current_checkbox_recyclerView.isVisible = false
                            current_checkbox_edit_text.isVisible = false
                            checkList.forEach {
                                mTodoViewModel.deleteList(it)

                                madapter.notifyDataSetChanged()
                            }
                            checkList.clear()
                            madapter.submitList(null)
                            layoutListItem = "Note"

                        }
                        .show()
                }
            }



            return@setOnNavigationItemSelectedListener true
        }
//
//
//        view.current_bottom_nav.menu.findItem(R.id.gallery_item).setOnMenuItemClickListener {
//
//            return@setOnMenuItemClickListener true
//        }

        view.current_checkbox_edit_text.setEndIconOnClickListener {
            Log.d("Tagoo", checkList.size.toString())
            if (checkList.size == 0) {
                val mName = current_editText_checkbox.text.toString()
                val isChecked = false


                if (mName == "") {
                    Toast.makeText(
                        requireContext(),
                        "Please fill out all fields.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val newListData = TaskList(
                        mName,
                        isChecked,
                        0,

                        args.currentItem.ToDoNoteId
                    )

                    val adapterList = itemList + newListData
                    madapter.submitList(adapterList)
                    current_editText_checkbox.setText("")
                    checkList.add(newListData)
                }
            } else {
                val mName = current_editText_checkbox.text.toString()
                val isChecked = false


                if (mName == "") {
                    Toast.makeText(
                        requireContext(),
                        "Please fill out all fields.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val newListData = TaskList(
                        mName,
                        isChecked,
                        0,
                        args.currentItem.ToDoNoteId
                    )
                    val adapterList = checkList + newListData
                    checkList.add(newListData)
                    madapter.submitList(adapterList)
                    current_editText_checkbox.setText("")
                }
            }

        }

        madapter.currentList.forEach {
            view.checkbox_recyclerView.get(j).check_box_completed.setOnCheckedChangeListener { checkBox, isChecked ->


                if (isChecked) {
                    view.checkbox_recyclerView.get(j).text_view_name.paint.isStrikeThruText = true
                }

                j++;

            }
        }


        view.current_submit_check.setOnClickListener {
            val data = Data.Builder().putInt(addFragment.NOTIFICATION_ID, 0).build()
            val currentTime = System.currentTimeMillis()
            Log.d("Time Tag", currentFinalTime.toString())
            val delay = currentFinalTime - currentTime
            if (currentFinalTime != 0L) {
                Log.d("Delay", delay.toString())
                scheduleNotification(delay, data)
            }
            if (layoutListItem == "Note") {
                updateItem()
            } else if (layoutListItem == "Check") {
                if (compareList == checkList.size) {
                    insertListToDb()
                } else {
                    insertNewListDb()
                }


            }


        }

        view.current_back_nav_btn.setOnClickListener {
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }



        return view
    }

    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


                val itemToDelete = madapter.currentList.get(viewHolder.adapterPosition)
                //val listToDelete = adapter.adapterList[viewHolder.adapterPosition]
                //adapter.adapterList.remove(listToDelete)
//                val listItem = adapter.adapterList[viewHolder.adapterPosition].dataList


                val adapterList = checkList - itemToDelete
                checkList.remove(itemToDelete)
                mTodoViewModel.deleteList(madapter.currentList.get(viewHolder.adapterPosition))
                madapter.submitList(adapterList)
                Log.d("Checking","Content: ${checkList}")





//                adapter.notifyItemRemoved(viewHolder.adapterPosition)
//
//
//
//                mToDoViewModel.deleteAllTasks(adapter.dataList[store].ToDoNoteId)


//                Log.d("Taggg",viewHolder.adapterPosition.toString())
//                val somethingAgain = viewHolder.adapterPosition


                //  val something = adapter.adapterList[viewHolder.position].dataList

                //listToDelete.notifyDataSetChanged()


                //adapter.notifyItemRemoved(adapter.adapterList[viewHolder.adapterPosition])


                //restoreDeletedData(viewHolder.itemView,itemToDelete,viewHolder.adapterPosition)
            }
        }
        val itemToTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemToTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun insertNewListDb() {
        mTodoViewModel.deleteAllTasks(args.currentItem.ToDoNoteId)
        val adapterNewList = arrayListOf<TaskList>()
        var i = 0


        madapter.currentList.forEach {

            var newTask = TaskList(
                it.name,
                current_checkbox_recyclerView.get(i).check_box_completed.isChecked,
                0,
                args.currentItem.ToDoNoteId
            )
            i++;
            adapterNewList.add(newTask)
        }

        adapterNewList.forEach {
            mTodoViewModel.insertList(it)
        }

        val newData = ToDoData(
            args.currentItem.id,
            current_titleEt.text.toString(),
            mShareViewModel.parsePriority(pickedPriority),
            current_descEt.text.toString(),
            currentFinalDate,
            currentFinalTime,
            imageUri.toString(),
            args.currentItem.ToDoNoteId
        )

        mTodoViewModel.insertData(newData)
        findNavController().navigate(R.id.action_updateFragment_to_listFragment)


    }

    private fun checkPermissionForImage() {
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED)
            && (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED)
        ) {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                permission,
                1001
            ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            requestPermissions(
                permissionWrite,
                1002
            ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
        } else {
            pickImageFromGallery()
            requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1003) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "camera permission granted", Toast.LENGTH_LONG)
                    .show()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 1888)
            } else {
                Toast.makeText(requireContext(), "camera permission denied", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
        requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
    }

    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes =
            ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                current_imageNote.setImageURI(it)
                imageUri = it
                current_imageNote.isVisible = true
                requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 1888) {
            val photo: Bitmap = data!!.extras!!["data"] as Bitmap
            current_imageNote.setImageURI(getImageUri(requireContext(), photo))
            imageUri = getImageUri(requireContext(), photo)
        }

        requireActivity().current_bottom_nav?.selectedItemId = R.id.note_item
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUri = Uri.parse(args.currentItem.imageUri)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_save) {
            updateItem()
            val data = Data.Builder().putInt(addFragment.NOTIFICATION_ID, 0).build()
            val currentTime = System.currentTimeMillis()
            val delay = currentFinalTime - currentTime
            scheduleNotification(delay, data)
        } else if (item.itemId == R.id.menu_delete) {
            confirmItemRemoval()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mTodoViewModel.deleteItem(args.currentItem)
            mTodoViewModel.deleteAllTasks(args.currentItem.ToDoNoteId)
            Toast.makeText(
                requireContext(),
                "Successfully Removed: ${args.currentItem.title}",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete '${args.currentItem.title}'?")
        builder.setMessage("Are you sure you want to remove '${args.currentItem.title}'?")
        builder.create().show()
    }

    private fun scheduleNotification(delay: Long, data: Data) {
        Log.d("TAG11", "Delay: $delay")
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

        val instanceWorkManager = WorkManager.getInstance(requireContext())
        instanceWorkManager.beginUniqueWork(
            addFragment.NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE,
            notificationWork
        ).enqueue()
    }


    private fun updateItem() {
        val curr_mTitle = current_titleEt.text.toString()
        val curr_mDescription = current_descEt.text.toString()
        val curr_mDate = currentFinalDate
        val curr_mTime = currentFinalTime


        val validation = mShareViewModel.verifyDataFromUser(
            curr_mTitle,
            curr_mDescription,
            curr_mDate.toString(),
            curr_mTime.toString()
        )
        if (validation) {
            val updateItem = ToDoData(
                args.currentItem.id,
                curr_mTitle,
                mShareViewModel.parsePriority(pickedPriority),
                curr_mDescription,
                currentFinalDate,
                currentFinalTime,
                imageUri.toString(),
                args.currentItem.ToDoNoteId
            )
            mTodoViewModel.updateData(updateItem)
            Toast.makeText(requireContext(), "Successfully Updated!", Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT)
                .show()
        }


    }


    private fun parsePriority(priority: Priority): Int {

        return when (priority) {
            Priority.HIGH -> 0
            Priority.MEDIUM -> 1
            Priority.LOW -> 2
        }

    }


    private fun insertListToDb() {


        if (madapter.currentList.size != 0) {
            val adapterNewList = arrayListOf<TaskList>()
            var i = 0
            checkList.forEach { item ->

                val taskList = TaskList(
                    item.name,
                    current_checkbox_recyclerView[i].check_box_completed.isChecked,
                    item.Taskid,
                    args.currentItem.ToDoNoteId
                )
                i++
                adapterNewList.add(taskList)
                Toast.makeText(requireContext(), "List Added", Toast.LENGTH_SHORT).show()


            }



            adapterNewList.forEach {
                mTodoViewModel.insertList(it)
            }
            Toast.makeText(requireContext(), "List Added", Toast.LENGTH_SHORT).show()

            val newData = ToDoData(
                args.currentItem.id,
                current_titleEt.text.toString(),
                mShareViewModel.parsePriority(pickedPriority),
                current_descEt.text.toString(),
                currentFinalDate,
                currentFinalTime,
                imageUri.toString(),
                args.currentItem.ToDoNoteId
            )

            mTodoViewModel.updateData(newData)
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
    }


    private fun restoreDeletedData(view: View, deletedItem: TaskList, position: Int) {

        val snackBar = Snackbar.make(
            view,
            "Deleted '${deletedItem.name}'",
            Snackbar.LENGTH_LONG
        )

        snackBar.setAction("Undo") {
            mTodoViewModel.insertList(deletedItem)


        }
        snackBar.show()


    }


}
