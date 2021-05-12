package com.example.tourapp.views

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
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
    private var categoryFilter: PlaceListViewModel.FilterCategory = PlaceListViewModel.FilterCategory.NONE

    private var customList = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setDrawerEnabled(false)

        customList = arguments?.get("CustomList") as Boolean ?: false

        this.setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_place_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.placelist_menu, menu)

        if(customList) {
            val addItem = menu.findItem(R.id.opt_add_place)
            addItem.isEnabled = false
            addItem.isVisible = false
        }

        val searchItem = menu.findItem(R.id.opt_search_place).actionView as SearchView
        searchItem.maxWidth = Int.MAX_VALUE
        searchItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.myAdapter.filter.filter(newText)
                return false
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_add_place -> {
                view?.let {
                    val bundle: Bundle = Bundle()
                    bundle.putStringArrayList("listCodes", viewModel.listCodes)
                    Navigation.findNavController(it).navigate(R.id.action_placeListFragment_to_placeAddFragment, bundle)
                }
            }
            R.id.filter_by_current_user-> {
                categoryFilter = PlaceListViewModel.FilterCategory.USERID
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.filter_high_rate->{
                categoryFilter = PlaceListViewModel.FilterCategory.HIGHRATE
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.filter_low_rate->{
                categoryFilter = PlaceListViewModel.FilterCategory.LOWRATE
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.filter_by_prefs->{
                categoryFilter = PlaceListViewModel.FilterCategory.USERPREFS
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.none_filter-> {
                categoryFilter = PlaceListViewModel.FilterCategory.NONE
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceListViewModel::class.java)

        this.arrayListTags = (activity as MainActivity).arrayListTags
        viewModel.user = (activity as MainActivity).user

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_place_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //direction integers: -1 for up, 1 for down, 0 will always return false
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(viewModel.descargas >= Constants.MAX_DATABASE_ITEMS) {
                        viewModel.loadNewData()
                    }
                    Toast.makeText((activity as MainActivity), "endOfScroll", Toast.LENGTH_SHORT).show()
                }
            }
        })
        /*recyclerView = recycler_user_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }*/
        //configSpinner()

        //val customList = arguments?.get("CustomList") as Boolean

        viewModel.placeIndex = 0
        viewModel.descargas = 0

        if(!customList) {
            viewModel.getPlaceList(arrayListOf())
        }
        else {
            val nameBar = arguments?.get("nombre") as String
            (activity as MainActivity).supportActionBar?.title = nameBar

            val listCodes = arguments?.get("ListCodes") as ArrayList<String>
            viewModel.getPlaceList(listCodes)
        }
    }

    override fun onStart() {
        super.onStart()
        configSpinner()
    }

    private fun configSpinner() {

        arrayAdapter = ArrayAdapter((context as MainActivity), android.R.layout.simple_spinner_item, arrayListTags)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner_places_filter.adapter = arrayAdapter
        spinner_places_filter.setSelection(0)
        positionSpinner = 0
        spinner_places_filter.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?,
                                        pos: Int, arg3: Long) {

                val msupplier: String = spinner_places_filter.selectedItem.toString()
                if (positionSpinner != pos) {
                    if (pos >= 0 && pos < arrayListTags.size) {
                        positionSpinner = pos
                        //viewModel.filterPlaceList(viewModel.listPlace, pos)
                        viewModel.filterByCategory(pos, categoryFilter)
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