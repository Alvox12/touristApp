package com.example.tourapp.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
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
        this.setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_place_data, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.placedata_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_edit_place -> {
                view?.let {
                    //Navigation.findNavController(it).navigate(R.id.action_placeListFragment_to_placeAddFragment)
                }
            }
        }

        return super.onOptionsItemSelected(item)
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

        if(viewModel.place.placeLatitude == 0.0 && viewModel.place.placeLongitude == 0.0) {
            btn_map.isEnabled = false
            btn_map.visibility = View.GONE
        }
        
        btn_map.setOnClickListener {
            openMap()
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

    private fun openMap() {

        //val permissionGranted = (activity as MainActivity).checkMapServices()
        //val permissionGranted = (activity as MainActivity).mLocationPermissionGranted

        //val gmmIntentUri = Uri.parse("geo:37.7749,-122.4192?q=" + Uri.encode("1st & Pike, Seattle"))


        val gmmIntentUri = Uri.parse("geo:40.416775,-3.703790")
        //val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps")

        val mapIntent = Intent((context as MainActivity), MapsActivity245::class.java)

        /*mapIntent.putExtra("Place", viewModel.place)
        mapIntent.putExtra("AddPlace", false)
        mapIntent.putExtra("MyUser", viewModel.user)*/

        mapIntent.putExtra("AddNewPlace", false)
        mapIntent.putExtra("Place", viewModel.place)
        mapIntent.putExtra("MyUser", viewModel.user)

        mapIntent.resolveActivity((activity as MainActivity).packageManager)?.let {
            //startActivityForResult(mapIntent, 123)
            startActivity(mapIntent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == requestCode) {
            //Obtenemos resultados de data
        }
    }

    fun destroyObserver() {
        viewModel.imagesDownloaded.removeObserver(this.observerImageDownloaded)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.deleteCommentListener()
    }
}