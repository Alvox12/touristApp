package com.example.tourapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.adapter.RecyclerCreateListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceCreateListViewModel
import kotlinx.android.synthetic.main.fragment_place_create_list.*
import kotlinx.android.synthetic.main.fragment_place_list.*
import kotlinx.android.synthetic.main.fragment_place_list.recycler_place_view
import kotlinx.android.synthetic.main.fragment_place_list.spinner_places_filter


class PlaceCreateListFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceCreateListFragment()
    }

    private lateinit var viewModel: PlaceCreateListViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    lateinit var arrayAdapter: ArrayAdapter<String>
    var arrayListTags: ArrayList<String> = arrayListOf()
    var positionSpinner: Int = 0

    private lateinit var observerListUploaded : Observer<Boolean>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        return inflater.inflate(R.layout.fragment_place_create_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceCreateListViewModel::class.java)
        this.arrayListTags = (activity as MainActivity).arrayListTags
        val user = (activity as MainActivity).user
        viewModel.user = user

        if(viewModel.user.userType == Constants.ADMIN)
            viewModel.idCreator = arguments?.get("ListCreator") as String
        else
            viewModel.idCreator = viewModel.user.userId

        viewModel.placeIndex = 0
        viewModel.descargas = 0

        viewModel.newList = arguments?.get("newList") as Boolean
        viewModel.listCodes = arguments?.get("listCodes") as ArrayList<String>

        if(!viewModel.newList) {
            viewModel.listSelected = arguments?.get("listSelected") as ArrayList<String>
            viewModel.listName = arguments?.get("listName") as String
            viewModel.listId = arguments?.get("listId") as String
        }

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_place_view.apply {
            layoutManager = manager
            adapter = viewModel.myAdapter
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //direction integers: -1 for up, 1 for down, 0 will always return false
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(viewModel.descargas >= Constants.MAX_DATABASE_ITEMS) {
                        viewModel.loadNewData()
                    }
                }
            }
        })

        btn_add_list.setOnClickListener {
            if(viewModel.newList)
                    (activity as MainActivity).addCustomListName(viewModel)
            else
                viewModel.uploadListPlace(viewModel.listName)
        }


        observerListUploaded = Observer {
            if(it) {
                Toast.makeText((context as MainActivity), "Lista dada de alta", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).onBackPressed()
            }
        }

        viewModel.listUploaded.observe(viewLifecycleOwner, observerListUploaded)
        viewModel.getPlaceList()

        configSpinner()
    }

    private fun configSpinner() {

        arrayAdapter = ArrayAdapter((context as MainActivity), android.R.layout.simple_spinner_item, arrayListTags)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner_places_filter.adapter = arrayAdapter
        spinner_places_filter.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?,
                                        pos: Int, arg3: Long) {

                val msupplier: String = spinner_places_filter.selectedItem.toString()
                if (positionSpinner != pos) {
                    if (pos >= 0 && pos < arrayListTags.size) {
                        positionSpinner = pos
                        viewModel.filterPlaceList(pos)
                    }
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {

            }
        })

    }

    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.deletePlaceListener()
        viewModel.listUploaded.removeObserver(observerListUploaded)
        (activity as MainActivity).setDrawerEnabled(true)
    }

}