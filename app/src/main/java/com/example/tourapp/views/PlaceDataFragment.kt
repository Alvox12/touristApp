package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.PlaceDataViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_delete_place.view.*
import kotlinx.android.synthetic.main.dialog_logout.view.*
import kotlinx.android.synthetic.main.dialog_score_place.*
import kotlinx.android.synthetic.main.fragment_place_data.*


class PlaceDataFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceDataFragment()
    }

    private lateinit var viewModel: PlaceDataViewModel
    private lateinit var observerImageDownloaded : Observer<Boolean>
    private lateinit var observerFavPlace : Observer<Boolean>

    /*Indica si se ha entrado por primera vez a la vista de datos del lugar desde otro fragmento
    * una vez se pone a true indica que ya se ha configurado por primera vez la vista de datos del lugar.*/
    private var favInitialSetup = false

    /*Menu barra superior*/
    private lateinit var menu: Menu

    /*Icono favorito*/
    private lateinit var favIcon: MenuItem
    private var previo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_data, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.placedata_menu, menu)
        this.menu = menu
        favIcon = this.menu.findItem(R.id.opt_fav_place)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /*Se establecen las acciones que cada elemento del menu de la barra superior ha de realizar*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            /*Opcion editar lugar*/
            R.id.opt_edit_place -> {
                view?.let {
                    val bundle = Bundle()
                    bundle.putSerializable("Place", viewModel.place)
                    Navigation.findNavController(it).navigate(R.id.action_placeDataFragment_to_placeModify2Fragment, bundle)
                }
            }
            /*Opcion eliminar lugar*/
            R.id.opt_delete_place -> {
                //Dialog eliminar lugar
                showDialogDelete()
            }
            /*Opcion añadir lugar a favoritos*/
            R.id.opt_fav_place -> {
                //Dependiendo del valor del booleanoo favoritePlace Eliminaremos o Subiremos un elemento a la lista
                if(!viewModel.favoritePlace) { //Si no esta en la lista SUBIMOS ELEMENTO
                    viewModel.favUploadDelete(true)
                }
                else { //Si esta ELIMINAMOS ELEMENTO
                    viewModel.favUploadDelete(false)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        /*Solo se puede editar o eliminar lugar si eres ADMIN o el creador de ese lugar*/
        if(viewModel.user.userType != Constants.ADMIN && viewModel.user.userId != viewModel.place.placeCreator) {
            menu.findItem(R.id.opt_delete_place).isEnabled = false
            menu.findItem(R.id.opt_delete_place).isVisible = false

            menu.findItem(R.id.opt_edit_place).isEnabled = false
            menu.findItem(R.id.opt_edit_place).isVisible = false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(PlaceDataViewModel::class.java)

        val place = arguments?.get("Place") as Place
        viewModel.place = place
        viewModel.latLng = viewModel.getLatLng(place.placeCoordinates)
       //val previo = arguments?.get("Previous") as String


        val user = (activity as MainActivity).user
        viewModel.user = user


        (context as MainActivity).toolbar.title = viewModel.place.placeName

        tv_placeName.text = viewModel.place.placeName
        tv_placeDescription.text = viewModel.place.placeDescription
        place_rating_bar.rating = viewModel.place.placeScore.toFloat()
        place_rating_bar.isActivated = false

        sliderView.setSliderAdapter(viewModel.sliderAdapter)

        viewModel.getFavListId()
        viewModel.getCommentList()

        viewModel.getImages(viewModel.place.placePictures)
    }

    override fun onResume() {
        super.onResume()

        favInitialSetup = false
        initSetup()

        val navController = view?.let { Navigation.findNavController(it) }
        // Instead of String any types of data can be used
        if (navController != null) {
            /*Si vuelve hacia atras desde otro fragmento se establece que favInitialSetup ha de estar en true*/
            navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("key")?.observe(viewLifecycleOwner) { bundle ->
                bundle.clear()
                favInitialSetup = false
            }
        }
    }


    private fun initSetup() {

        /*Boton lleva a la vista de comentarios*/
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


        /*Al tocar barra puntuacion abre ventana flotante para modificar puntuacion*/
        place_rating_bar.setOnTouchListener OnTouchListener@{ view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                (activity as MainActivity).ratePlace(viewModel)
            }
            return@OnTouchListener true
        }

        /*Si la longitud y latitud valen 0 ocultar opcion mostrar posicion*/
        if(viewModel.latLng.latitude == 0.0 && viewModel.latLng.longitude == 0.0) {
            btn_map.isEnabled = false
            btn_map.visibility = View.GONE
        }

        /*Abrir mapa tras pulsar boton*/
        btn_map.setOnClickListener {
            openMap()
        }

        /*Si lugar se ha añadido o eliminado de lista de favoritos en la BBDD avisar al usuario y cambiar icono barra superior*/
        observerFavPlace = Observer {
            if(it) {
                //Cambiamos icono de favoritos
                favIcon.icon = ContextCompat.getDrawable((activity as MainActivity), R.drawable.ic_baseline_favorite_checked_24)!!
                //Si ya se ha realizado configuracion inicial avisar al usuario del cambio
                if(favInitialSetup)
                    Toast.makeText((activity as MainActivity), "Lugar añadido a favoritos", Toast.LENGTH_SHORT).show()
                else
                    favInitialSetup = true
            }
            else {
                //Cambiamos icono de favoritos
                favIcon.icon = ContextCompat.getDrawable((activity as MainActivity), R.drawable.ic_baseline_favorite_unchecked_24)!!
                //Si ya se ha realizado configuracion inicial avisar al usuario del cambio
                if(favInitialSetup)
                    Toast.makeText((activity as MainActivity), "Lugar eliminado de favoritos", Toast.LENGTH_SHORT).show()
                else
                    favInitialSetup = true
            }
        }

        viewModel.favPlaceLiveData.observe(viewLifecycleOwner, observerFavPlace)

        /*Si las imagenes se han descargado correctamente mostrarlas en pantalla*/
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

    /**Funcion para abrir actividad de google maps en las coordenadas especificadas y con el nombre del lugar*/
    private fun openMap() {

        val gmmIntentUri = Uri.parse("geo:40.416775,-3.703790")
        //val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps")

        viewModel.favPlaceLiveData.removeObserver(this.observerFavPlace)
        viewModel.deleteCommentListener()

        val mapIntent = Intent((context as MainActivity), MapsActivity::class.java)

        mapIntent.putExtra("AddNewPlace", false)
        //mapIntent.putExtra("Place", viewModel.place)
        mapIntent.putExtra("Name", viewModel.place.placeName)
        mapIntent.putExtra("Lat", viewModel.latLng.latitude)
        mapIntent.putExtra("Lng", viewModel.latLng.longitude)
        mapIntent.putExtra("MyUser", viewModel.user)

        mapIntent.resolveActivity((activity as MainActivity).packageManager)?.let {
            //startActivityForResult(mapIntent, 123)
            startActivity(mapIntent)
        }

    }


    /**Funcion para eliminar lugar de la base de datos*/
    fun deletePlace() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(viewModel.place.placeId)
        placeRef.removeValue().addOnCompleteListener {
            if(it.isSuccessful) {

                if(viewModel.place.placePictures.isNotBlank()) {
                    val storageRef = FirebaseStorage.getInstance().getReference(viewModel.place.placePictures)
                    storageRef.delete().addOnCompleteListener { it2 ->
                        if (it2.isSuccessful) {
                            Toast.makeText(
                                (activity as MainActivity),
                                "Lugar eliminado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }
    }

    private fun showDialogDelete() {
        val dialogBuilder = AlertDialog.Builder((activity as MainActivity))
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_place, null)
        val b = dialogBuilder.setView(dialogView).create()

        dialogView.btn_delete_cancel.setOnClickListener{
            b.dismiss()
        }

        dialogView.btn_delete_accept.setOnClickListener{
            deletePlace()
            b.dismiss()
            (activity as MainActivity).onBackPressed()
        }

        b.setCancelable(false)
        b.show()
    }

    private fun destroyObserver() {
        viewModel.imagesDownloaded.removeObserver(this.observerImageDownloaded)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.favPlaceLiveData.removeObserver(this.observerFavPlace)
        viewModel.deleteCommentListener()
    }

}