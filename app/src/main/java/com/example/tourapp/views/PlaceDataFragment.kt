package com.example.tourapp.views

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceDataViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_place_data.*

class PlaceDataFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceDataFragment()
    }

    private lateinit var viewModel: PlaceDataViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_data, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(PlaceDataViewModel::class.java)
        viewModel.place = arguments?.get("Place") as Place


        (context as MainActivity).toolbar.title = viewModel.place.placeName

        tv_placeName.text = viewModel.place.placeName
        tv_placeDescription.text = viewModel.place.placeDescription
        place_rating_bar.rating = viewModel.place.placeScore.toFloat()
    }

}