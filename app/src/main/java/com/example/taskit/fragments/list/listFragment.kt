package com.example.taskit.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.taskit.R
import com.example.taskit.data.models.ToDoData
import com.example.taskit.data.viewmodel.ToDoViewModel
import com.example.taskit.fragments.SharedViewModel
import com.example.taskit.fragments.list.adapter.ListAdapter
import com.example.taskit.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.maxkeppeler.bottomsheets.options.DisplayMode
import com.maxkeppeler.bottomsheets.options.Option
import com.maxkeppeler.bottomsheets.options.OptionsSheet
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class listFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()

    private val adapter: ListAdapter by lazy { ListAdapter(requireActivity(), mToDoViewModel ) }
//    private var layout:String = "Grid"


    private val mShareViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val recyclerView = view.recyclerView
        recyclerView.adapter = adapter


        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)


        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(recyclerView)

        mToDoViewModel.readFromDataStore.observe(requireActivity(), {
            if (it == "Grid") {
                recyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else if (it == "List") {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        })

        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            mShareViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })
        mShareViewModel.emptyDatabase.observe(viewLifecycleOwner, Observer {
            showEmptyDatabaseViews(it)
        })

        view.floatingActionButton.setOnClickListener {
            adapter.clearContextualActionMode()
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        view.settings_main_btn.setOnClickListener {

            OptionsSheet().show(requireActivity()) {
                title("Settings")

                with(
                    Option(R.drawable.ascending_sort, "SortBy High Priority"),
                    Option(R.drawable.sort_descending, "SortBy Low Priority"),
                    Option(R.drawable.layout, "List Layout"),
                    Option(R.drawable.grid, "Grid Layout")


                )

                showButtons()
                displayMode(DisplayMode.LIST)
                //displayMode(displayMde = List)

                onPositive { index: Int, option: Option ->
                    if (index == 0) {
                        mToDoViewModel.sortByHighPriority.observe(this, Observer {
                            adapter.setData(it)
                        })

                    } else if (index == 1) {
                        mToDoViewModel.sortByLowPriority.observe(this, Observer {
                            adapter.setData(it)
                        })

                    } else if (index == 2) {
                        mToDoViewModel.saveToDataStore("List")
                    } else if (index == 3) {
                        mToDoViewModel.saveToDataStore("Grid")
                    }
                }

            }

        }

        view.search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                //after the change calling the method and passing the search input
                filter(editable.toString())
            }
        })


        // To Display All Menu Item On Toolbar
        setHasOptionsMenu(true)

        hideKeyboard(requireActivity())



        return view
    }

    private fun filter(text: String) {
        var searchQuery: String = text
        searchQuery = "%$searchQuery%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer { list ->
            list?.let {
                adapter.setData(it)
            }

        })

    }


    private fun showEmptyDatabaseViews(emptyDatabase: Boolean) {
        if (emptyDatabase) {
            view?.no_data_imageView?.visibility = View.VISIBLE
            view?.no_data_textView?.visibility = View.VISIBLE
            view?.curvedArrow?.visibility = View.VISIBLE
        } else {
            view?.no_data_imageView?.visibility = View.INVISIBLE
            view?.no_data_textView?.visibility = View.INVISIBLE
            view?.curvedArrow?.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId == R.id.menu_delete_all){
//            confirmRemoval()
//        }
//        else if(item.itemId == R.id.menu_priority_high){
//
//            mToDoViewModel.sortByHighPriority.observe(this, Observer {
//                adapter.setData(it)
//            })
//
//        }
//        else if(item.itemId == R.id.menu_priority_low){
//
//            mToDoViewModel.sortByLowPriority.observe(this, Observer {
//                adapter.setData(it)
//            })
//
//        }
//
//
//        return super.onOptionsItemSelected(item)
//    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


                val itemToDelete = adapter.dataList[viewHolder.adapterPosition]
                //val listToDelete = adapter.adapterList[viewHolder.adapterPosition]
                //adapter.adapterList.remove(listToDelete)
//                val listItem = adapter.adapterList[viewHolder.adapterPosition].dataList

                mToDoViewModel.deleteItem(itemToDelete)
                val store = viewHolder.adapterPosition

                adapter.notifyItemRemoved(viewHolder.adapterPosition)



                mToDoViewModel.deleteAllTasks(adapter.dataList[store].ToDoNoteId)


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


    private fun restoreDeletedData(view: View, deletedItem: ToDoData, position: Int) {

        val snackBar = Snackbar.make(
            view,
            "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )

        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)


        }
        snackBar.show()


    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.clearContextualActionMode()
    }


    // Show Alert Dialog To Confirm Deletion Of All Items
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(), "Successfully Removed Everything!", Toast.LENGTH_SHORT)
                .show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete Everything?")
        builder.setMessage("Are you sure you want to remove Everything?")
        builder.create().show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        var searchQuery: String = query
        searchQuery = "%$searchQuery%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer { list ->
            list?.let {
                adapter.setData(it)
            }

        })

    }

//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//
//        val menuInflater = requireActivity().menuInflater
//        menuInflater.inflate(R.menu.recycler_view_menu,menu)
//    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }
}