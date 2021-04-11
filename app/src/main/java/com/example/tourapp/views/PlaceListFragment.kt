package com.example.tourapp.views

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceListViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_place_list.*


class PlaceListFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceListFragment()
    }

    private lateinit var viewModel: PlaceListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    lateinit var arrayAdapter: ArrayAdapter<String>
    var arrayListTags: ArrayList<String> = arrayListOf()
    var positionSpinner: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        this.setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_place_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.placelist_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_add_place -> {
                view?.let {
                    Navigation.findNavController(it).navigate(R.id.action_placeListFragment_to_placeAddFragment)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceListViewModel::class.java)

        this.arrayListTags = (activity as MainActivity).arrayListTags

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_place_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }
        /*recyclerView = recycler_user_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }*/
        configSpinner()

        val customList = arguments?.get("CustomList") as Boolean

        if(!customList)
            viewModel.getPlaceList(arrayListOf())
        else {
            val nameBar = arguments?.get("nombre") as String
            (activity as MainActivity).supportActionBar?.title = nameBar

            val listCodes = arguments?.get("ListCodes") as ArrayList<String>
            viewModel.getPlaceList(listCodes)
        }
    }


    private fun configSpinner() {

        arrayAdapter = ArrayAdapter((context as MainActivity), android.R.layout.simple_spinner_item, arrayListTags)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner_places_filter.adapter = arrayAdapter
        spinner_places_filter.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?,
                                        pos: Int, arg3: Long) {

                val msupplier: String = spinner_places_filter.selectedItem.toString()
                if(positionSpinner != pos) {
                    if (pos >= 0 && pos < arrayListTags.size) {
                        positionSpinner = pos
                        viewModel.filterPlaceList(pos)
                    }
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {

            }
        })
        /*spinner_places_filter.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                if(positionSpinner != pos) {
                    if (pos >= 0 && pos < arrayListTags.size) {
                        positionSpinner = pos
                        viewModel.filterPlaceList(pos)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }*/
    }

    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
       viewModel.deletePlaceListener()
        (activity as MainActivity).setDrawerEnabled(true)
    }

}