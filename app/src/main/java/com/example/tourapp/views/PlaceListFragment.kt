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

    /*Posicion por defecto del spinner de filtros por etiqueta*/
    var positionSpinner: Int = 0
    /*Filtro por defecto de categorias*/
    private var categoryFilter: PlaceListViewModel.FilterCategory = PlaceListViewModel.FilterCategory.NONE

    /*Booleano que indica si la lista actual es la lista general de lugares o una de las listas
    * personales del usuario logueado*/
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

        /*Configuracion barra de busqueda por texto*/
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

    /**
     * En funcion de la opcion del menu superior seleccionada se aplicará un filtro u otro o puede añadirse un lugar nuevo*/
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

        /*Hay un tope de 30 descargas si se desea descargar mas lugares hay que bajar hasta abajo de la pantalla
        * donde se avisará a una funcion que descargue los lugares restantes si son menos de 30 u otros 30 más*/
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

    /**Configuracion inicial del spinner que en funcion de la posicion del mismo se
     * filtrará por una etiqueta u otra (o ninguna que es la posicion 0)*/
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
    }

    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
       viewModel.deletePlaceListener()
        (activity as MainActivity).setDrawerEnabled(true)
    }

}