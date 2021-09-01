package com.example.tourapp.views

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.viewModel.CustomPlaceListViewModel
import com.example.tourapp.viewModel.PlaceListViewModel
import kotlinx.android.synthetic.main.fragment_custom_place_list.*
import kotlinx.android.synthetic.main.fragment_place_list.*
import kotlinx.android.synthetic.main.fragment_place_list.recycler_place_view
import kotlinx.android.synthetic.main.fragment_place_list.spinner_places_filter

class CustomPlaceListFragment : Fragment() {

    companion object {
        fun newInstance() = CustomPlaceListFragment()
    }

    private lateinit var viewModel: CustomPlaceListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    lateinit var arrayAdapter: ArrayAdapter<String>
    var arrayListTags: ArrayList<String> = arrayListOf()

    var positionSpinner: Int = 0
    private var categoryFilter: CustomPlaceListViewModel.FilterCategory = CustomPlaceListViewModel.FilterCategory.NONE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        this.setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_custom_place_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.placelist_menu, menu)

        val addItem = menu.findItem(R.id.opt_add_place)
        addItem.isEnabled = false
        addItem.isVisible = false

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
            R.id.filter_by_current_user-> {
                categoryFilter = CustomPlaceListViewModel.FilterCategory.USERID
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.filter_high_rate->{
                categoryFilter = CustomPlaceListViewModel.FilterCategory.HIGHRATE
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.filter_low_rate->{
                categoryFilter = CustomPlaceListViewModel.FilterCategory.LOWRATE
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.filter_by_prefs->{
                categoryFilter = CustomPlaceListViewModel.FilterCategory.USERPREFS
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
            R.id.none_filter-> {
                categoryFilter = CustomPlaceListViewModel.FilterCategory.NONE
                viewModel.filterByCategory(positionSpinner, categoryFilter)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CustomPlaceListViewModel::class.java)

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
                }
            }
        })

        viewModel.placeIndex = 0
        viewModel.descargas = 0

        val nameBar = arguments?.get("nombre") as String
        (activity as MainActivity).supportActionBar?.title = nameBar

        if(viewModel.user.userType == Constants.ADMIN)
            viewModel.idCreator = arguments?.get("ListCreator") as String
        else
            viewModel.idCreator = viewModel.user.userId

        val listCode = arguments?.get("ListCodes") as String
        viewModel.listCode = listCode
        viewModel.getCustomListCodes()

        fab_add_place.setOnClickListener {
            view.let {
                if (it != null) {
                    viewModel.initialDownload = true
                    val bundle = Bundle()
                    bundle.putBoolean("newList", false)
                    bundle.putString("listId", viewModel.listCode)
                    bundle.putString("listName", nameBar)

                    if(viewModel.user.userType == Constants.ADMIN)
                        bundle.putString("ListCreator", viewModel.idCreator)

                    bundle.putStringArrayList("listSelected", viewModel.keysPlaces)
                    bundle.putStringArrayList("listCodes", arrayListOf())
                    Navigation.findNavController(it).navigate(R.id.action_customPlaceListFragment_to_placeCreateListFragment2, bundle)
                }
            }
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
        spinner_places_filter.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?,
                                        pos: Int, arg3: Long) {

                val msupplier: String = spinner_places_filter.selectedItem.toString()
                if (positionSpinner != pos) {
                    if (pos >= 0 && pos < arrayListTags.size) {
                        positionSpinner = pos
                        viewModel.filterByCategory(pos, categoryFilter)
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
        (activity as MainActivity).setDrawerEnabled(true)
    }

}