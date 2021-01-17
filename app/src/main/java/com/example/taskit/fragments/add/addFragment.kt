package com.example.taskit.fragments.add


import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.taskit.R
import com.example.taskit.data.models.TaskList
import com.example.taskit.data.models.ToDoData
import com.example.taskit.data.viewmodel.ToDoViewModel
import com.example.taskit.fragments.SharedViewModel
import com.example.taskit.fragments.list.adapter.TaskListAdapter
import com.example.taskit.notifications.NotifyWork
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.maxkeppeler.bottomsheets.input.InputSheet
import com.maxkeppeler.bottomsheets.input.type.InputSpinner
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.checklist_layout.view.*
import kotlinx.android.synthetic.main.custom_dialog.view.*
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.coroutines.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


// This is the fragment where new Tasks are added
class addFragment : androidx.fragment.app.Fragment() {



    var itemList: MutableList<TaskList> = ArrayList()


    lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    var pickedPriority: String = "Low Priority"
    var remindValue:Boolean = false

    private var imageUri: Uri = Uri.EMPTY
    var finalDate = 0L
    var finalTime = 0L
    var mNoteId: Int = 0

    var layoutListItem: String = "Note"

    // Initializing my View Model
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mShareViewModel: SharedViewModel by viewModels()

    private var counter = 0;

    // Initializing Check List Adapter
    private val adapter: TaskListAdapter by lazy {
        TaskListAdapter(
            mToDoViewModel
        )
    }

    // To manage notifications
    companion object {

        const val NOTIFICATION_ID = "appName_notification_id"
        const val NOTIFICATION_WORK = "appName_notification_work"

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        setHasOptionsMenu(true)


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)


        view.spinnerCategory.onItemSelectedListener = mShareViewModel.listener

        // Recycler view layout to display the checkList
        val recyclerView = view.checkbox_recyclerView


        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Storing value of mNoteId , which will be linked with the single task that user creates
        mToDoViewModel.readIntDataStore.observe(requireActivity(), {
            Log.d("TAG21", it.toString())
            mNoteId = it
        })


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)



        back_nav_btn.setOnClickListener {
            // Sends the user back to list fragment
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }


        // This get's executed when , any option from the bottom nav menu is clicked
        bottom_nav.setOnNavigationItemSelectedListener {

            if (it.itemId == R.id.priority_item) {

                // Showing a bottom sheet to - let user set priority for the task
                InputSheet().show(requireActivity()) {
                    title("Choose Priority")
                    content("Here you can prioritize your task on it's importance !")
                    cancelableOutside(false)

                    var indexSpinner = -1

                    with(InputSpinner() {
                        required()

                        label("Pick Priortity")


                        options(mutableListOf("High Priority", "Medium Priority", "Low Priority"))


                        changeListener { it ->
                            indexSpinner = it
                        }

                    })
                    onPositive { result ->


                        // On clicking ok , priority is set as the picked index from the list and later passed inside the database
                        if (indexSpinner == 0) {
                            pickedPriority = "High Priority"
                        } else if (indexSpinner == 1) {
                            pickedPriority = "Medium Priority"
                        } else if (indexSpinner == 2) {
                            pickedPriority = "Low Priority"
                        } else {
                            pickedPriority = "Low Priority"
                        }

                        Toast.makeText(requireContext(), pickedPriority, Toast.LENGTH_SHORT).show()

                        // Changing selected bottom nav item back to note_item
                        requireActivity().bottom_nav?.selectedItemId = R.id.note_item

                    }


                    onNegative {
                        // Changing selected bottom nav item back to note_item
                        requireActivity().bottom_nav?.selectedItemId = R.id.note_item
                    }
                }

            } else if (it.itemId == R.id.gallery_item) {
                if(layoutListItem == "Check"){
                    Toast.makeText(requireContext(),"Images are only supported in Note/Description Mode",Toast.LENGTH_SHORT).show()

                    Handler().postDelayed( { requireActivity().bottom_nav?.selectedItemId = R.id.check_box_item }, 500)
                }
                else {
                    checkPermissionForImage()
                    requireActivity().bottom_nav?.selectedItemId = R.id.note_item
                }
                // Taking Image Permissions

            } else if (it.itemId == R.id.camera_item) {

                if(layoutListItem == "Check"){
                    Toast.makeText(requireContext(),"Images are only supported in Note/Description Mode",Toast.LENGTH_SHORT).show()


                    Handler().postDelayed( { requireActivity().bottom_nav?.selectedItemId = R.id.check_box_item }, 500)
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
                        )
                    } else {
                        var cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 1888);
                    }
                }

            } else if (it.itemId == R.id.check_box_item) {
                if (layoutListItem == "Note") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are You Sure ?")
                        .setMessage("Do you want to shift to CheckList mode ? Your note will be discarded!")
                        .setNegativeButton("Cancel") { dialog, which ->
                            // Respond to negative button press
                        }
                        .setPositiveButton("Ok") { dialog, which ->

                            descEt.isVisible = false
                            checkbox_recyclerView.isVisible = true
                            checkbox_edit_text.isVisible = true
                            layoutListItem = "Check"

                        }
                        .show()
                }
            } else if (it.itemId == R.id.note_item) {
                if (layoutListItem == "Check") {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are You Sure ?")
                        .setMessage("Do you want to shift to Note mode ? Your CheckList will be discarded!")
                        .setNegativeButton("Cancel") { dialog, which ->
                            // Respond to negative button press
                        }
                        .setPositiveButton("Ok") { dialog, which ->

                            descEt.isVisible = true
                            checkbox_recyclerView.isVisible = false
                            checkbox_edit_text.isVisible = false
                            layoutListItem = "Note"

                        }
                        .show()
                }
            }

            return@setOnNavigationItemSelectedListener true
        }


        val df = SimpleDateFormat("dd-MMM-yyyy")
        val date = df.format(Calendar.getInstance().time)
        date_view.text = date


        checkbox_edit_text.setEndIconOnClickListener {
            insertListToRv()
        }


        submit_check.setOnClickListener {
            if (layoutListItem == "Note") {
                // Used when there is no checklist in the todo
                insertDataToDb()
            } else {
                // used when there is a checklist in the todo
                insertListToDb()
            }

            val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()
            val currentTime = System.currentTimeMillis()
            val delay = finalTime - currentTime
            if (finalTime == 0L) {
                Toast.makeText(requireContext(), "No Time Set", Toast.LENGTH_SHORT).show()
            } else {
                scheduleNotification(delay, data)
            }

        }

        // Setting a dialog to set time , for the task

        timeSet_btn.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireActivity()).create()
            val dialogView = this.layoutInflater.inflate(R.layout.custom_dialog, null)


//
//            dialogView.dateEdit.setText((sdfDate.format(mShareViewModel.calendar.time)))

            dialogView.dateEdit.setOnClickListener {


                dateSetListener =
                    DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->


                        mShareViewModel.calendar.set(Calendar.YEAR, year)
                        mShareViewModel.calendar.set(Calendar.MONTH, month)
                        mShareViewModel.calendar.set(Calendar.DAY_OF_MONTH, day)

                        var myformat = "dd/MM/yy"

                        var sdfDate = SimpleDateFormat(myformat)
                        dialogView.dateEdit.setText((sdfDate.format(mShareViewModel.calendar.time)))


                    }
                val datePickerDialog = DatePickerDialog(
                    requireActivity(),
                    dateSetListener,
                    mShareViewModel.calendar.get(Calendar.YEAR),
                    mShareViewModel.calendar.get(Calendar.MONTH),
                    mShareViewModel.calendar.get(Calendar.DAY_OF_MONTH)
                )

                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }


//
////
//            // For Time Picker
//            updateTime()

            dialogView.timeEdt.setOnClickListener {


                // Setting a time listener , by building the Time Picker Dialog
                timeSetListener =
                    TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, min: Int ->

                        // Converting the time picker to appropriate format needed in which is hours and mins
                        mShareViewModel.calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        mShareViewModel.calendar.set(Calendar.MINUTE, min)


                        val myformat = "h:mm a"

                        val sdf = SimpleDateFormat(myformat)
                        // Finally Displaying The Text
                        dialogView.timeEdt.setText((sdf.format(mShareViewModel.calendar.time)))


                    }
                // Creating Instance Of Time Picker Dialog
                val timePickerDialog = TimePickerDialog(
                    activity,
                    timeSetListener,
                    mShareViewModel.calendar.get(Calendar.HOUR_OF_DAY),
                    mShareViewModel.calendar.get(Calendar.MINUTE),
                    false
                )
                // Displaying The Dialog
                timePickerDialog.show()


            }




            dialogView.okBtn.setOnClickListener {

                if (!(dialogView.dateEdit.text.isNullOrEmpty() || dialogView.timeEdt.text.isNullOrEmpty())) {
                    finalTime = mShareViewModel.calendar.time.time
                    finalDate = mShareViewModel.calendar.time.time

                    remindValue = dialogView.customRemind.isChecked

                    if(!remindValue) {
                        if (finalTime < System.currentTimeMillis()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Oops!")
                                .setMessage("You can't set a notification ,for previous date/time ")

                                .setPositiveButton("Ok") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .show()
                        } else {
                            dialogBuilder.dismiss()
                        }
                    }
                    else {
                        if ((finalTime-1800000) < System.currentTimeMillis()) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Oops!")
                                .setMessage("You can't set a notification reminder ,for previous date/time ")

                                .setPositiveButton("Ok") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .show()
                        } else {
                            dialogBuilder.dismiss()
                        }
                    }

                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Fields can't be left empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }
            dialogView.cancelBtn.setOnClickListener {
                dialogBuilder.dismiss()
            }


            dialogBuilder.apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                setView(dialogView)
                setCancelable(true)
                show()
            }


        }


    }

    private fun insertListToDb() {


        var isConnected:Boolean

        val connectivityManager:ConnectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        if((wifiConn !=null && wifiConn.isConnected || (mobileConn != null && mobileConn.isConnected) )){
            isConnected = true
        }
        else{
            isConnected = false
        }



        if (adapter.currentList.size != 0) {
            var adapterNewList = arrayListOf<TaskList>()
            var i = 0;
            adapter.currentList.forEach {
                val taskList = TaskList(
                    it.name,
                    checkbox_recyclerView.get(i).check_box_completed.isChecked,
                    0,
                    mNoteId
                )

                i++;
                adapterNewList.add(taskList)
                Toast.makeText(requireContext(), "List Added", Toast.LENGTH_SHORT).show()

            }


//            adapterNewList.forEach {
//                mToDoViewModel.insertList(it)
//            }

            if (finalTime != 0L) {
                if(isConnected) {
                    if(!remindValue) {
                        val outputFmt =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        outputFmt.timeZone = TimeZone.getTimeZone("UTC")

                        Log.d("Date: ", "${outputFmt.format(finalTime)}")


                        CoroutineScope(Dispatchers.Default).launch {


                            val id = OneSignal.getDeviceState()?.userId

                            val client: OkHttpClient = OkHttpClient().newBuilder()
                                .build()
                            val mediaType: MediaType? = MediaType.parse("application/json")
                            val body: RequestBody = RequestBody.create(
                                mediaType,
                                "{\n  \"app_id\": \"2bfdec01-56ed-45d6-bb5c-da5412b25f45\",\n  \"include_player_ids\": [\"${id}\"],\n  \"data\": {\"foo\": \"bar\"},\n  \"contents\": {\"en\": \"${titleEt.text.toString()}\"},\n  \"headings\": {\"en\": \"Task Is Due! \"},\n  \"priority\": 10,\n  \"android_accent_color\": \"356cf9\",\n \"delayed_option\": \"send_after\",\n \"send_after\": \"${outputFmt.format(
                                    mShareViewModel.calendar.time
                                )}\",\n  \"android_channel_id\": \"f79697ff-1772-4310-9bbb-80bf7969267b\" \n \n}"
                            )
                            val request: Request = Request.Builder()
                                .url("https://onesignal.com/api/v1/notifications")
                                .method("POST", body)
                                .addHeader(
                                    "Authorization",
                                    "Basic {\"YTQyNmMyZjctNDQ3ZC00ZDJmLWJkODAtYmU1MjY4ZmY4ZjBm\"}"
                                )
                                .addHeader("Content-Type", "application/json")
                                .addHeader(
                                    "Cookie",
                                    "__cfduid=dc5d614eeaf73d53794c169310ece29f01610380890"
                                )
                                .build()
                            val response: Response = client.newCall(request).execute()


                        }
                    }
                    else {

                        Log.d("Remind: ", remindValue.toString())
                        // Setting Notification for 30 Mins Before , For The User

                        // Before 30 Mins Notification
                        val outputFmt30 =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        outputFmt30.timeZone = TimeZone.getTimeZone("UTC")

                        val id30 = OneSignal.getDeviceState()?.userId

                        val client: OkHttpClient = OkHttpClient().newBuilder()
                            .build()
                        val mediaType: MediaType? = MediaType.parse("application/json")
                        val body: RequestBody = RequestBody.create(
                            mediaType,
                            "{\n  \"app_id\": \"2bfdec01-56ed-45d6-bb5c-da5412b25f45\",\n  \"include_player_ids\": [\"${id30}\"],\n  \"data\": {\"foo\": \"bar\"},\n  \"contents\": {\"en\": \"30 minutes left for your task\"},\n  \"headings\": {\"en\": \"Task To Finish!\"},\n  \"priority\": 10,\n  \"android_accent_color\": \"356cf9\",\n \"delayed_option\": \"send_after\",\n \"send_after\": \"${outputFmt30.format(
                                (finalTime - 1800000)
                            )}\",\n  \"android_channel_id\": \"f79697ff-1772-4310-9bbb-80bf7969267b\" \n \n}"
                        )
                        val request: Request = Request.Builder()
                            .url("https://onesignal.com/api/v1/notifications")
                            .method("POST", body)
                            .addHeader(
                                "Authorization",
                                "Basic {\"YTQyNmMyZjctNDQ3ZC00ZDJmLWJkODAtYmU1MjY4ZmY4ZjBm\"}"
                            )
                            .addHeader("Content-Type", "application/json")
                            .addHeader(
                                "Cookie",
                                "__cfduid=dc5d614eeaf73d53794c169310ece29f01610380890"
                            )
                            .build()
                        val response: Response = client.newCall(request).execute()

                        // Exact Time Notification
                        CoroutineScope(Dispatchers.IO).launch {

                            val outputFmt =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            outputFmt.timeZone = TimeZone.getTimeZone("UTC")

                            val id = OneSignal.getDeviceState()?.userId

                            val clientExact: OkHttpClient = OkHttpClient().newBuilder()
                                .build()
                            val mediaTypeExact: MediaType? = MediaType.parse("application/json")
                            val bodyExact: RequestBody = RequestBody.create(
                                mediaTypeExact,
                                "{\n  \"app_id\": \"2bfdec01-56ed-45d6-bb5c-da5412b25f45\",\n  \"include_player_ids\": [\"${id}\"],\n  \"data\": {\"foo\": \"bar\"},\n  \"contents\": {\"en\": \"Is your Task finished?\"},\n  \"headings\": {\"en\": \"Task Is Due!\"},\n  \"priority\": 10,\n  \"android_accent_color\": \"356cf9\",\n \"delayed_option\": \"send_after\",\n \"send_after\": \"${outputFmt.format(
                                    finalTime
                                )}\",\n  \"android_channel_id\": \"f79697ff-1772-4310-9bbb-80bf7969267b\" \n \n}"
                            )
                            val requestExact: Request = Request.Builder()
                                .url("https://onesignal.com/api/v1/notifications")
                                .method("POST", bodyExact)
                                .addHeader(
                                    "Authorization",
                                    "Basic {\"YTQyNmMyZjctNDQ3ZC00ZDJmLWJkODAtYmU1MjY4ZmY4ZjBm\"}"
                                )
                                .addHeader("Content-Type", "application/json")
                                .addHeader(
                                    "Cookie",
                                    "__cfduid=dc5d614eeaf73d53794c169310ece29f01610380890"
                                )
                                .build()
                            val responseExact: Response =
                                clientExact.newCall(requestExact).execute()

                        }
                    }
                }
                else {

                }

            }

          //  Toast.makeText(requireContext(), "List Added", Toast.LENGTH_SHORT).show()

            if(finalTime == 0L) {

                val validation = mShareViewModel.verifyDataFromUser(
                    titleEt.text.toString(),
                    descEt.text.toString(),
                    "12",
                    "12"
                )




                    val newData = ToDoData(
                        0,
                        titleEt.text.toString(),
                        mShareViewModel.parsePriority(pickedPriority),
                        descEt.text.toString(),
                        finalDate,
                        finalTime,
                        imageUri.toString(),
                        mNoteId
                    )

                    mToDoViewModel.insertData(newData)
                    mNoteId = mNoteId + 1
                    mToDoViewModel.saveIntDataStore(mNoteId)
                    adapterNewList.forEach {
                        mToDoViewModel.insertList(it)
                    }
                    findNavController().navigate(R.id.action_addFragment_to_listFragment)


            }
            else{
                if (!isConnected) {
                    //Toast.makeText(requireContext(),"Setting Notifications Require Internet, Please activate your network.",Toast.LENGTH_LONG).show()
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder.setMessage("Setting Notifications Require Internet, Please activate your network")
                        .setCancelable(false)
                        .setPositiveButton(
                            "Connect",
                            DialogInterface.OnClickListener { dialog, which ->
                                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            })
                        .setNegativeButton(
                            "Cancel",
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            })

                    builder.show()
                }
                else {


                        val newData = ToDoData(
                            0,
                            titleEt.text.toString(),
                            mShareViewModel.parsePriority(pickedPriority),
                            descEt.text.toString(),
                            finalDate,
                            finalTime,
                            imageUri.toString(),
                            mNoteId
                        )

                        mToDoViewModel.insertData(newData)
                        mNoteId = mNoteId + 1
                        mToDoViewModel.saveIntDataStore(mNoteId)
                        adapterNewList.forEach {
                            mToDoViewModel.insertList(it)
                        }
                        findNavController().navigate(R.id.action_addFragment_to_listFragment)

                }
            }
        }
    }


    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                requireActivity().bottom_nav?.selectedItemId = R.id.note_item
            } else {
                pickImageFromGallery()
                requireActivity().bottom_nav?.selectedItemId = R.id.note_item

            }
        }
    }


    // Picking image from gallery - todo - fix this for pixel devices
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(
            intent,
            1000
        )
        requireActivity().bottom_nav?.selectedItemId = R.id.note_item
    }

    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes =
            ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.getContentResolver(),
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
                imageNote.setImageURI(it)
                imageUri = it
                imageNote.isVisible = true
                requireActivity().bottom_nav?.selectedItemId = R.id.note_item
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 1888) {
            val photo: Bitmap = data!!.extras!!["data"] as Bitmap
            imageNote.setImageURI(getImageUri(requireContext(), photo))

            imageUri = getImageUri(requireContext(), photo)
        }

        requireActivity().bottom_nav?.selectedItemId = R.id.note_item
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ((requestCode == 1001 || requestCode == 1002) && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 1003) {
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


    // scheduling the notification at the time set by user
    private fun scheduleNotification(delay: Long, data: Data) {
        Log.d("TAG11", "Delay: " + delay.toString())
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

        val instanceWorkManager = WorkManager.getInstance(requireContext())
        instanceWorkManager.beginUniqueWork(
            NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE,
            notificationWork
        ).enqueue()
    }

    private fun insertListToRv() {
        val mName = editText_checkbox.text.toString()
        val isChecked = false

        if (mName == "") {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT)
                .show()
        } else {

            val newListData = TaskList(
                mName,
                isChecked,
                0,
                mNoteId
            )
            val adapterList = itemList + newListData
            itemList.add(newListData)
            adapter.submitList(adapterList)
            Log.d("TAG2","Current List: ${adapter.currentList}")
            counter++
            editText_checkbox.setText("")
        }


    }

    private fun insertDataToDb() {


//
//        finalTime = mShareViewModel.calendar.time.time

//        finalDate = mShareViewModel.calendar.time.time




        var isConnected:Boolean

        val connectivityManager:ConnectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        if((wifiConn !=null && wifiConn.isConnected || (mobileConn != null && mobileConn.isConnected) )){
            isConnected = true
        }
        else{
            isConnected = false
        }



        CoroutineScope(Dispatchers.Default).launch {

            // val time = (sdf.format(mShareViewModel.calendar.time))

//            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true



            Log.d("Connected?",isConnected.toString())

            if (finalTime != 0L) {
                if(isConnected) {
                    if(!remindValue) {
                        val outputFmt =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        outputFmt.timeZone = TimeZone.getTimeZone("UTC")

                        Log.d("Date: ", "${outputFmt.format(finalTime)}")


                        val id = OneSignal.getDeviceState()?.userId

                        val client: OkHttpClient = OkHttpClient().newBuilder()
                            .build()
                        val mediaType: MediaType? = MediaType.parse("application/json")
                        val body: RequestBody = RequestBody.create(
                            mediaType,
                            "{\n  \"app_id\": \"2bfdec01-56ed-45d6-bb5c-da5412b25f45\",\n  \"include_player_ids\": [\"${id}\"],\n  \"data\": {\"foo\": \"bar\"},\n  \"contents\": {\"en\": \"${titleEt.text.toString()}\"},\n  \"headings\": {\"en\": \"Task Is Due!\"},\n  \"priority\": 10,\n  \"android_accent_color\": \"356cf9\",\n \"delayed_option\": \"send_after\",\n \"send_after\": \"${outputFmt.format(
                                finalTime
                            )}\",\n  \"android_channel_id\": \"f79697ff-1772-4310-9bbb-80bf7969267b\" \n \n}"
                        )
                        val request: Request = Request.Builder()
                            .url("https://onesignal.com/api/v1/notifications")
                            .method("POST", body)
                            .addHeader(
                                "Authorization",
                                "Basic {\"YTQyNmMyZjctNDQ3ZC00ZDJmLWJkODAtYmU1MjY4ZmY4ZjBm\"}"
                            )
                            .addHeader("Content-Type", "application/json")
                            .addHeader(
                                "Cookie",
                                "__cfduid=dc5d614eeaf73d53794c169310ece29f01610380890"
                            )
                            .build()
                        val response: Response = client.newCall(request).execute()
                    }
                    else {
                        Log.d("Remind: ", remindValue.toString())
                        // Setting Notification for 30 Mins Before , For The User


                            // Before 30 Mins Notification
                            val outputFmt30 =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            outputFmt30.timeZone = TimeZone.getTimeZone("UTC")

                            val id30 = OneSignal.getDeviceState()?.userId

                            val client: OkHttpClient = OkHttpClient().newBuilder()
                                .build()
                            val mediaType: MediaType? = MediaType.parse("application/json")
                            val body: RequestBody = RequestBody.create(
                                mediaType,
                                "{\n  \"app_id\": \"2bfdec01-56ed-45d6-bb5c-da5412b25f45\",\n  \"include_player_ids\": [\"${id30}\"],\n  \"data\": {\"foo\": \"bar\"},\n  \"contents\": {\"en\": \"30 minutes left for your task\"},\n  \"headings\": {\"en\": \"Task To Finish!\"},\n  \"priority\": 10,\n  \"android_accent_color\": \"356cf9\",\n \"delayed_option\": \"send_after\",\n \"send_after\": \"${outputFmt30.format(
                                    (finalTime - 1800000)
                                )}\",\n  \"android_channel_id\": \"f79697ff-1772-4310-9bbb-80bf7969267b\" \n \n}"
                            )
                            val request: Request = Request.Builder()
                                .url("https://onesignal.com/api/v1/notifications")
                                .method("POST", body)
                                .addHeader(
                                    "Authorization",
                                    "Basic {\"YTQyNmMyZjctNDQ3ZC00ZDJmLWJkODAtYmU1MjY4ZmY4ZjBm\"}"
                                )
                                .addHeader("Content-Type", "application/json")
                                .addHeader(
                                    "Cookie",
                                    "__cfduid=dc5d614eeaf73d53794c169310ece29f01610380890"
                                )
                                .build()
                            val response: Response = client.newCall(request).execute()

                            // Exact Time Notification

                        CoroutineScope(Dispatchers.IO).launch {
                            val outputFmt =
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            outputFmt.timeZone = TimeZone.getTimeZone("UTC")

                            val id = OneSignal.getDeviceState()?.userId

                            val clientExact: OkHttpClient = OkHttpClient().newBuilder()
                                .build()
                            val mediaTypeExact: MediaType? = MediaType.parse("application/json")
                            val bodyExact: RequestBody = RequestBody.create(
                                mediaTypeExact,
                                "{\n  \"app_id\": \"2bfdec01-56ed-45d6-bb5c-da5412b25f45\",\n  \"include_player_ids\": [\"${id}\"],\n  \"data\": {\"foo\": \"bar\"},\n  \"contents\": {\"en\": \"Is your Task finished?\"},\n  \"headings\": {\"en\": \"Task Is Due!\"},\n  \"priority\": 10,\n  \"android_accent_color\": \"356cf9\",\n \"delayed_option\": \"send_after\",\n \"send_after\": \"${outputFmt.format(
                                    finalTime
                                )}\",\n  \"android_channel_id\": \"f79697ff-1772-4310-9bbb-80bf7969267b\" \n \n}"
                            )
                            val requestExact: Request = Request.Builder()
                                .url("https://onesignal.com/api/v1/notifications")
                                .method("POST", bodyExact)
                                .addHeader(
                                    "Authorization",
                                    "Basic {\"YTQyNmMyZjctNDQ3ZC00ZDJmLWJkODAtYmU1MjY4ZmY4ZjBm\"}"
                                )
                                .addHeader("Content-Type", "application/json")
                                .addHeader(
                                    "Cookie",
                                    "__cfduid=dc5d614eeaf73d53794c169310ece29f01610380890"
                                )
                                .build()
                            val responseExact: Response =
                                clientExact.newCall(requestExact).execute()
                        }
                        
                    }
                }
                else {

                     //   Toast.makeText(requireContext(),"Setting Notifications Require Internet, Please activate your network.",Toast.LENGTH_LONG).show()
                }

            }
        }

        if(finalTime == 0L){

            val mTitle = titleEt.text.toString()
            val mDescription = descEt.text.toString()
            val mDate = 0
            val mTime = 0

            val validation = mShareViewModel.verifyDataFromUser(
                mTitle,
                mDescription,
                mDate.toString(),
                mTime.toString()
            )


            if (validation) {
                val newData = ToDoData(
                    0,
                    mTitle,
                    mShareViewModel.parsePriority(pickedPriority),
                    mDescription,
                    finalDate,
                    finalTime,
                    imageUri.toString(),
                    mNoteId

                )
                mToDoViewModel.insertData(newData)
                mNoteId = mNoteId + 1
                mToDoViewModel.saveIntDataStore(mNoteId)
                Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addFragment_to_listFragment)
            }
            else {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT)
                    .show()
            }


        }
        else {
            if (!isConnected) {
                //Toast.makeText(requireContext(),"Setting Notifications Require Internet, Please activate your network.",Toast.LENGTH_LONG).show()
                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setMessage("Setting Notifications Require Internet, Please activate your network")
                    .setCancelable(false)
                    .setPositiveButton("Connect", DialogInterface.OnClickListener { dialog, which ->
                        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })

                builder.show()
            } else {
                val mTitle = titleEt.text.toString()
                val mDescription = descEt.text.toString()
                val mDate = 0
                val mTime = 0

                val validation = mShareViewModel.verifyDataFromUser(
                    mTitle,
                    mDescription,
                    mDate.toString(),
                    mTime.toString()
                )


                if (validation) {
                    val newData = ToDoData(
                        0,
                        mTitle,
                        mShareViewModel.parsePriority(pickedPriority),
                        mDescription,
                        finalDate,
                        finalTime,
                        imageUri.toString(),
                        mNoteId

                    )
                    mToDoViewModel.insertData(newData)
                    mNoteId = mNoteId + 1
                    mToDoViewModel.saveIntDataStore(mNoteId)
                    Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_addFragment_to_listFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please fill out all fields.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }


        }



    }


}


