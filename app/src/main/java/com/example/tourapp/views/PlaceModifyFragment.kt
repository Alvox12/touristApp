package com.example.tourapp.views

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.adapter.RecyclerTagListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceModifyViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_place_modify.*
import androidx.lifecycle.Observer
import java.util.*
import kotlin.collections.ArrayList

class PlaceModifyFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceModifyFragment()
    }

    private lateinit var viewModel: PlaceModifyViewModel
    /*Objeto del lugar a modificar*/
    private lateinit var place: Place

    private val PICK_IMAGE_REQUEST: Int = 1
    private var imagePath: Uri? = null
    private var imageExtension: String? = null

    /*Numeros de imagenes seleccionadas para subir a la BBDD*/
    private var cnt_images = 0
    /*Coordenadas del lugar a subir (si no se desean añadir se dejaran en 0,0)*/
    private var latLng: LatLng = LatLng(0.0, 0.0)

    private lateinit var et_name_watcher: TextWatcher
    private lateinit var et_info_watcher: TextWatcher

    private lateinit var dialogBox: Dialog
    private lateinit var animalAdapter: RecyclerTagListAdapter

    /*Reacciona a si el lugar ha sido actualizado en la BBDD*/
    private lateinit var observerPlaceUploaded : androidx.lifecycle.Observer<Boolean>

    /*Ruta del lugar donde se guardaran las imagenes almacenadas en la BBDD*/
    private var pathImage = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_modify, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaceModifyViewModel::class.java)
        /*Obtenemos del lugar anterior la informacion del lugar*/
        place = arguments?.get("Place") as Place

        viewModel.id_lugar = place.placeId

        viewModel.arrayListTags = (activity as MainActivity).arrayListTags.clone() as ArrayList<String>
        viewModel.arrayListTags.removeAt(0)

        viewModel.listTagsSelected = ArrayList(Collections.nCopies(viewModel.arrayListTags.size, false))
        getSelectedTags()

        viewModel.user = (activity as MainActivity).user

        et_place_name.setText(place.placeName)
        et_place_info.setText(place.placeDescription)
        if(place.placeCoordinates != "0.0,0.0") {
            getLatLng()
        }

        viewModel.initData()
        initSetup()
    }

    /**Obtiene las coordenadas del lugar en cuestion*/
    fun getLatLng() {
        if(place.placeCoordinates != "") {
            val list: List<String> = place.placeCoordinates.split(",")
            val lat = list.first().toDoubleOrNull()
            val lng = list.last().toDoubleOrNull()

            if(lat != null && lng != null) {
                latLng = LatLng(lat, lng)
            }
        }
    }

    /**Se establacen los tags del lugar a editar*/
    private fun getSelectedTags() {
        val array = place.arrayTags
        for(num in array.iterator()) {
            viewModel.listTagsSelected?.set(num-1, true)
        }
    }

    private fun initSetup() {

        /*Muestra ventana flotante donde se seleccionan los tags para el lugar*/
        btn_tags.setOnClickListener {
            showDialogTags()
        }

        /*Campo de texto para el nombre del lugar que si tras modificarlo se encuentra vacio
        * mostrará un texto de error*/
        et_name_watcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (et_place_name.text.isBlank()) {
                    et_place_name.error = "Tienes que escribir un nombre"
                } else {
                    et_place_name.error = null
                }
            }
        }
        et_place_name.addTextChangedListener(et_name_watcher)

        /*Campo de texto para la descripcion del lugar que si tras modificarlo se encuentra vacio
        * mostrará un texto de error*/
        et_info_watcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (et_place_info.text.isBlank()) {
                    et_place_info.error = "Tienes que dar una descripción"
                } else {
                    et_place_info.error = null
                }
            }
        }
        et_place_info.addTextChangedListener(et_info_watcher)

        btn_map.setOnClickListener {
            openMap()
        }

        /*btn_images.setOnClickListener {
            if (!Utils.checkPermission(activity as MainActivity))
                view?.let { it1 -> Utils.askforPermission(it1) }
            else
                openFileChooser()
        }*/


        /*Boton para dar de alta la infromacion actualizada a la BBDD*/
        imgb_upload.setOnClickListener {
            darAltaLugar()
        }

        /*Si los datos han sido modificados y dados de alta se ejecuta lo de observerPlaceUploaded*/
        observerPlaceUploaded = Observer{
            if(it) {
                Toast.makeText((context as MainActivity), "Datos lugar modificados", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).onBackPressed()
            }
        }

        viewModel.placeUploaded.observe(viewLifecycleOwner, observerPlaceUploaded)
    }


    /**Da de alta la informacion sobre el lugar haya sido modificada o no*/
    private fun darAltaLugar() {
        val tags = getStringTags()
        /*Es necesario seleccionar al menos una etiqueta para el lugar*/
        if(tags == "") {
            Toast.makeText((context as MainActivity), "Has de seleccionar al menos una etiqueta", Toast.LENGTH_SHORT).show()
        }
        else {
            val name: String = et_place_name.text.toString()
            val info: String = et_place_info.text.toString()
            val latitude = this.latLng.latitude
            val longitude = this.latLng.longitude

            /*Es obligatorio que los campos de nombre y descripcion no se encuentren vacios*/
            if(name.isBlank()) {
                et_place_name.error = "Tienes que escribir un nombre"
            }
            if(info.isBlank()) {
                et_place_info.error = "Tienes que dar una descripción"
            }

            if(!name.isBlank() && !info.isBlank()) {

                et_place_name.error = null
                et_place_info.error = null

                if (this.cnt_images > 0)
                    this.pathImage = "Lugares/${viewModel.id_lugar}/"

                place.placeName = name
                place.placeDescription = info
                place.placeCoordinates = "${latitude},${longitude}"
                place.placeTags = tags
                //val myPlace = Place(viewModel.id_lugar, name, info, viewModel.user.userId, 5.0, this.pathImage, "${latitude},${longitude}", tags)
                viewModel.modifyPlace(place)
            }
        }
    }

    /**Toma los tags seleccionados y los convierte a un string para subirlos a la BBDD*/
    private fun getStringTags(): String {
        var msg = ""
        var first = true
        for((index, aux) in viewModel.listTagsSelected!!.withIndex()) {
            if(aux) {
                if(first) {
                    msg += (index+1)
                    first = false
                }
                else
                    msg += "," + (index+1)
            }
        }
        return msg
    }

    fun getIntTags(aux: String): ArrayList<Int> {
        val list: List<String> = aux.split(",")
        val listInt: ArrayList<Int> = arrayListOf()

        list.forEach { str ->
            listInt.add(Integer.parseInt(str))
        }

        return listInt
    }

    /**Funcion abre actividad google maps*/
    private fun openMap() {

        val gmmIntentUri = Uri.parse("geo:40.416775,-3.703790")
        //val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps")

        val mapIntent = Intent((context as MainActivity), MapsActivity::class.java)

        mapIntent.putExtra("AddNewPlace", true)
        mapIntent.putExtra("MyUser", viewModel.user)

        mapIntent.resolveActivity((activity as MainActivity).packageManager)?.let {
            startActivityForResult(mapIntent, Constants.OPEN_MAP_REQUEST)
        }

    }


    /**Muestra ventana flotante con todos los tags para seleccionar los que el
     * usuario desee*/
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
     * Obtenemos la imagen seleccionada o las coordenadas del mapa*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*Si el codigo es PICK_IMAGE_REQUEST significa que se ha seleccionado una imagen
       * si el codigo es OPEN_MAP_REQUEST significa que se ha vuelto de seleccionar unas
       * coordenadas de google maps*/
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null) {
            imagePath = data.data

            if(!(activity as MainActivity).validPicture(imagePath))
                Toast.makeText((context as MainActivity), "tam. maximo es ${Constants.ICON_MAX_SIZE4}x${Constants.ICON_MAX_SIZE4}px", Toast.LENGTH_SHORT).show()
            else {
                val type = imagePath?.let { activity?.contentResolver?.getType(it) }
                imageExtension = type?.substring(type.lastIndexOf('/') + 1)
                Toast.makeText((context as MainActivity), "Imagen seleccionada: $imageExtension", Toast.LENGTH_SHORT).show()

                viewModel.myMapPlaceImg[cnt_images] = imagePath
                viewModel.myMapImgExtension[cnt_images] = imageExtension
                cnt_images++

                if(cnt_images > 0) {
                    //tv_img_counter.text = "Imágenes seleccionadas: $cnt_images"
                    //tv_img_counter.visibility = View.VISIBLE
                    //btn_clear_images.isEnabled = true
                }

                //Introducimos en el text view nombre imagen a subir
                //tv_logo.text = autotv_userClient.text.toString().toLowerCase() + "_logo." + imageExtension
            }

        }
        else if(requestCode == Constants.OPEN_MAP_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            //Obtenemos resultados de data
            //val select_place = data.getSerializableExtra("Place") as Place
            val latitude = data.getSerializableExtra("Lat") as Double
            val longitude = data.getSerializableExtra("Lng") as Double
            this.latLng = LatLng(latitude, longitude)
        }
    }

    override fun onDestroyView() {
        et_place_name.removeTextChangedListener(et_name_watcher)
        et_place_info.removeTextChangedListener(et_info_watcher)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.placeUploaded.removeObserver(observerPlaceUploaded)
    }


}