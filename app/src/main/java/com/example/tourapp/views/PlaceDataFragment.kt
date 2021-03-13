package com.example.tourapp.views

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.tourapp.R
import com.example.tourapp.adapter.SliderAdapter
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceDataViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_place_data.*


class PlaceDataFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceDataFragment()
    }

    private lateinit var viewModel: PlaceDataViewModel
    private lateinit var observerImageDownloaded : Observer<Boolean>


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
        val previo = arguments?.get("Previous") as String


        val user = (activity as MainActivity).user
        viewModel.user = user

        (context as MainActivity).toolbar.title = viewModel.place.placeName

        tv_placeName.text = viewModel.place.placeName
        tv_placeDescription.text = viewModel.place.placeDescription
        place_rating_bar.rating = viewModel.place.placeScore.toFloat()
        place_rating_bar.isEnabled = false

        sliderView.setSliderAdapter(viewModel.sliderAdapter)

        viewModel.getCommentList()

        initSetup()

        if(previo == "Comments") {
            val arrayBitmap =arguments?.get("ImagesMap") as ArrayList<Bitmap>
            viewModel.myBitmapPlaceImg = arrayListToMutableMap(arrayBitmap)
            viewModel.imagesDownloaded.value = true
        }
        else
            viewModel.getImages(viewModel.place.placePictures)
    }


    private fun initSetup() {

        btn_comment.setOnClickListener {
            val bundle : Bundle = Bundle()
            bundle.putSerializable("Comments", viewModel.place)

            val arrayBitmap: ArrayList<Bitmap> = ArrayList()
            arrayBitmap.addAll(viewModel.myBitmapPlaceImg.values)

            bundle.putParcelableArrayList("ImagesMap", arrayBitmap)

            view.let {
                if (it != null) {
                    Navigation.findNavController(it).navigate(R.id.action_placeDataFragment_to_commentListFragment, bundle)
                }
            }
        }

        btn_rate.setOnClickListener {
            (activity as MainActivity).ratePlace(viewModel)
        }


        observerImageDownloaded = Observer {
            if(it) {
                if(viewModel.myBitmapPlaceImg.isNotEmpty()) {
                    ll_slider_view.visibility = View.VISIBLE
                    destroyObserver()
                    viewModel.setImagesSlider()
                }
            }
        }

        viewModel.imagesDownloaded.observe(viewLifecycleOwner, observerImageDownloaded)
    }

    private fun arrayListToMutableMap(arrayList: ArrayList<Bitmap>): MutableMap<Int, Bitmap> {

        val mutableMap: MutableMap<Int, Bitmap> = mutableMapOf()

        for(index in arrayList.indices) {
            mutableMap[index] = arrayList[index]
        }

        return mutableMap
    }

    fun destroyObserver() {
        viewModel.imagesDownloaded.removeObserver(this.observerImageDownloaded)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.deleteCommentListener()
    }
}