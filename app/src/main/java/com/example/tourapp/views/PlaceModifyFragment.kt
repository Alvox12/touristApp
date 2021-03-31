package com.example.tourapp.views

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tourapp.R
import com.example.tourapp.viewModel.PlaceModifyViewModel
import java.util.*
import kotlin.collections.ArrayList

class PlaceModifyFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceModifyFragment()
    }

    private lateinit var viewModel: PlaceModifyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_modify, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceModifyViewModel::class.java)
        viewModel.arrayListTags = (activity as MainActivity).arrayListTags
        viewModel.listTagsSelected = ArrayList(Collections.nCopies(viewModel.arrayListTags.size, false))
    }

}