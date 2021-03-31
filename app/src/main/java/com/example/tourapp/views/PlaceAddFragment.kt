package com.example.tourapp.views

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.adapter.RecyclerTagListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.Utils
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceAddViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.dialog_tags_place.*
import kotlinx.android.synthetic.main.dialog_tags_place.view.*
import kotlinx.android.synthetic.main.dialog_tags_place.view.rv_tags_list
import kotlinx.android.synthetic.main.fragment_place_add.*
import kotlinx.android.synthetic.main.fragment_place_add.btn_map
import kotlinx.android.synthetic.main.fragment_place_data.*


class PlaceAddFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceAddFragment()
    }

    private lateinit var viewModel: PlaceAddViewModel

    private val PICK_IMAGE_REQUEST: Int = 1
    private val OPEN_MAP_REQUEST : Int = 123
    private var imagePath: Uri? = null
    private var imageExtension: String? = null

    private var cnt_images = 0
    private var latLng: LatLng = LatLng(0.0, 0.0)

    private lateinit var dialogBox: Dialog
    private lateinit var animalAdapter: RecyclerTagListAdapter

    private lateinit var observerPlaceUploaded : Observer<Boolean>

    private var pathImage = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_add, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceAddViewModel::class.java)
        val arrayListTags = (activity as MainActivity).arrayListTags.clone() as ArrayList<String>
        arrayListTags.removeAt(0)
        viewModel.initData(arrayListTags)

        viewModel.user = (activity as MainActivity).user

        initSetup()
    }

    private fun initSetup() {
        btn_tags.setOnClickListener {
            showDialogTags()
        }

        /*btnUpload.setOnClickListener {
            //Solo puede subir una imagen cuando se crea un usuario de tipo Cliente.
            if(sp_userLevel.selectedItem.toString().toUpperCase() != Constants.CLIENTE) {
                Toast.makeText(activity?.applicationContext, "Solo puedes subir imagen si creas un usuario tipo Cliente", Toast.LENGTH_SHORT).show()
            }
            else {
                if (!Utils.checkPermission(activity as MainActivity))
                    Utils.askforPermission(view!!)
                else
                    openFileChooser()
            }
        }*/
        btn_images.setOnClickListener {
            if (!Utils.checkPermission(activity as MainActivity))
                view?.let { it1 -> Utils.askforPermission(it1) }
            else
                openFileChooser()
        }

        btn_map.setOnClickListener {
            openMap()
        }

        imgb_upload.setOnClickListener {
            darAltaLugar()
        }

        observerPlaceUploaded = Observer {
            if(it) {
                (activity as MainActivity).onBackPressed()
            }
        }

        viewModel.placeUploaded.observe(viewLifecycleOwner, observerPlaceUploaded)
    }


    private fun darAltaLugar() {
        val tags = getStringTags()
        if(tags == "") {
            Toast.makeText((context as MainActivity), "Has de seleccionar al menos una etiqueta", Toast.LENGTH_SHORT).show()
        }
        else {
            val name = et_place_name.text as String
            val info = et_place_info.text as String
            val latitude = this.latLng.latitude
            val longitude = this.latLng.longitude

            val myPlace = Place(viewModel.id_lugar, name, info, viewModel.user.userId, 5.0, this.pathImage, latitude, longitude,  getIntTags(tags))
            viewModel.uploadPlace(myPlace)
        }
    }

    private fun getStringTags(): String {
        var msg = ""
        for((index, aux) in viewModel.listTagsSelected!!.withIndex()) {
            if(aux) {
                msg += "," + (index+1)
            }
        }
        return msg
    }

    fun getIntTags(aux: String): ArrayList<Int> {
        val list: List<String> = aux.split(",")
        val listInt: ArrayList<Int> = arrayListOf()

        list.forEach {str ->
            listInt.add(Integer.parseInt(str))
        }

        return listInt
    }

    private fun openMap() {

        //val permissionGranted = (activity as MainActivity).checkMapServices()
        //val permissionGranted = (activity as MainActivity).mLocationPermissionGranted

        //val gmmIntentUri = Uri.parse("geo:37.7749,-122.4192?q=" + Uri.encode("1st & Pike, Seattle"))


        val gmmIntentUri = Uri.parse("geo:40.416775,-3.703790")
        //val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps")

        val mapIntent = Intent((context as MainActivity), MapsActivity245::class.java)

        mapIntent.putExtra("AddNewPlace", true)
        mapIntent.putExtra("MyUser", viewModel.user)

        mapIntent.resolveActivity((activity as MainActivity).packageManager)?.let {
            startActivityForResult(mapIntent, OPEN_MAP_REQUEST)
        }

    }


    private fun showDialogTags() {

        this.dialogBox = Dialog(context as MainActivity)

        this.dialogBox.setOnDismissListener {
            viewModel.getTagsSelected()
            btn_tags.isEnabled = true
        }

        this.dialogBox.setContentView(R.layout.dialog_tags_place)
        this.dialogBox.window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        this.animalAdapter = viewModel.tagsAdapter
        val animalsRecyclerView: RecyclerView = this.dialogBox.findViewById(R.id.rv_tags_list)//recycler view is defined in dialog box View
        animalsRecyclerView.layoutManager = LinearLayoutManager(context as MainActivity)
        animalsRecyclerView.adapter = this.animalAdapter

        dialogBox.show()
        btn_tags.isEnabled = false
        viewModel.setListAdapter()
    }

    /**
     * Abrimos el selector de imagenes*/
    private fun openFileChooser() {

        if(cnt_images < Constants.MAX_IMAGES_UPLOAD) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        else
            Toast.makeText((context as MainActivity), "El máximo de imágenes es ${Constants.MAX_IMAGES_UPLOAD}", Toast.LENGTH_SHORT).show()
    }


    /**
     * Obtenemos la imagen seleccionada*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null) {
            imagePath = data.data

            if(!(activity as MainActivity).validPicture(imagePath))
                Toast.makeText((context as MainActivity), "tam. maximo es ${Constants.ICON_MAX_SIZE}x${Constants.ICON_MAX_SIZE}px", Toast.LENGTH_SHORT).show()
            else {
                val type = imagePath?.let { activity?.contentResolver?.getType(it) }
                imageExtension = type?.substring(type.lastIndexOf('/') + 1)
                Toast.makeText((context as MainActivity), "Imagen seleccionada: $imageExtension", Toast.LENGTH_SHORT).show()

                viewModel.myMapPlaceImg[cnt_images] = imagePath
                viewModel.myMapImgExtension[cnt_images] = imageExtension
                cnt_images++

                //Introducimos en el text view nombre imagen a subir
                //tv_logo.text = autotv_userClient.text.toString().toLowerCase() + "_logo." + imageExtension
            }

        }
        else if(requestCode == OPEN_MAP_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            //Obtenemos resultados de data
            var select_place = data.getSerializableExtra("Place") as Place
            this.latLng = LatLng(select_place.placeLatitude, select_place.placeLongitude)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.placeUploaded.removeObserver(observerPlaceUploaded)
    }

}