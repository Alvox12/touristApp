package com.example.tourapp.views

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.viewModel.PlaceListViewModel
import kotlinx.android.synthetic.main.fragment_user_list.*

class PlaceListFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceListFragment()
    }

    private lateinit var viewModel: PlaceListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceListViewModel::class.java)

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_user_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        viewModel.getPlaceList()
    }

    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
       viewModel.deletePlaceListener()
        (activity as MainActivity).setDrawerEnabled(true)
    }

}