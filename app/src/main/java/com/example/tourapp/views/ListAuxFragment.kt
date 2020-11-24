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
import com.example.tourapp.adapter.RecyclerAuxAdapter
import com.example.tourapp.viewModel.ListAuxViewModel
import kotlinx.android.synthetic.main.fragment_list_aux.*

class ListAuxFragment : Fragment() {

    companion object {
        fun newInstance() = ListAuxFragment()
    }



    //val  values = arrayOf("one", "two" , "three", "four", "five" , "six", "seven", "eight", "nine", "ten")

    private lateinit var viewModel: ListAuxViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager
    //private lateinit var myAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        return inflater.inflate(R.layout.fragment_list_aux, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ListAuxViewModel::class.java)

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = recycler_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        //viewModel.addValue("JUJUUU")
        viewModel.getUserList()
    }

}