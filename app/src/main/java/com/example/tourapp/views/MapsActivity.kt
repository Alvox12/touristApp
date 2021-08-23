package com.example.tourapp.views

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tourapp.R
import com.example.tourapp.dataModel.User
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val NORMAL_ZOOM = 15f
    private val BIG_ZOOM = 17f

    //Booleano que indica si hay permiso localizacion
    private var mLocationPermissionsGranted: Boolean = false
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234

    private lateinit var currentLocation : Location
    private val TAG = "MapsActivity"

    //Indica si vamos a añadir una nueva localizacion o solo consultar
    private var addNewLocation: Boolean = false
    //private lateinit var place: Place
    private var placeName: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var user: User

    private lateinit var autocompleteFrag: AutocompleteSupportFragment

    private var AllMarkers: ArrayList<Marker> = arrayListOf()

    //Limite de comunidad de madrid
    private val comuMadridBounds = LatLngBounds(
            LatLng(40.227935, -4.515052),   // SW bounds
            LatLng(41.066457, -3.127167)    // NE bounds
    )

    //Limite madrid ciudad
    private val madridBounds = LatLngBounds(
            LatLng(40.296052, -3.935523),   // SW bounds
            LatLng(40.543836, -3.552375)    // NE bounds
    )

    private var firstMarker = true
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        initAutocompleteSupport()

        addNewLocation = intent.getSerializableExtra("AddNewPlace") as Boolean
        user = intent.getSerializableExtra("MyUser") as User

        if(!addNewLocation) {
            btn_save_location.isEnabled = false
            btn_save_location.visibility = View.GONE

            btn_add_location.isEnabled = false
            btn_add_location.visibility = View.GONE

            placeName = intent.getSerializableExtra("Name") as String
            latitude = intent.getSerializableExtra("Lat") as Double
            longitude = intent.getSerializableExtra("Lng") as Double
        }

        btn_gps.setOnClickListener {
            if(mLocationPermissionsGranted) {
                getLastKnownLocation()
            }
        }

        btn_save_location.isEnabled = false
        btn_save_location.setOnClickListener {
            onBackPressed()
        }

        btn_add_location.isEnabled = false
        btn_add_location.setOnClickListener {
            onBackPressed()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
        getLocationPermission()
    }


    private fun initAutocompleteSupport() {

        // Initialize Places.
        Places.initialize(applicationContext, resources.getString(R.string.google_maps_api_key))
        var placesClient = Places.createClient(this)

        this.autocompleteFrag = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        //autocompleteFrag.setTypeFilter(TypeFilter.GEOCODE)
        //autocompleteFrag.setLocationBias(RectangularBounds.newInstance(madridBounds))
        autocompleteFrag.setLocationRestriction(RectangularBounds.newInstance(madridBounds))
        autocompleteFrag.setCountry("ES")

        // Specify the types of place data to return.
        autocompleteFrag.setPlaceFields(listOf
        (com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME,com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,com.google.android.libraries.places.api.model.Place.Field.ADDRESS))

    }


    private fun init() {
        Log.d(TAG, "init: initializing")

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFrag.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: com.google.android.libraries.places.api.model.Place) {
                // Get info about the selected place.
                Log.d(TAG, "Place: ${place.name}, ${place.id}")

                moveCamera(place.latLng!!, NORMAL_ZOOM, place.name!!)
                hideSoftKeyboard()
            }

            override fun onError(status: Status) {
                // Handle the error.
                Log.i(TAG, "An error occurred: $status")
                Toast.makeText(this@MapsActivity, "Error al encontrar localización", Toast.LENGTH_SHORT).show()
            }

        })

        if(addNewLocation) {

           mMap.setOnMapLongClickListener { latlng ->
               if(this.firstMarker)
                   this.firstMarker = false
               else
                   marker.remove()

                marker = mMap.addMarker(MarkerOptions()
                        .position(latlng)
                        //.title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

               btn_save_location.isEnabled = true
               btn_add_location.isEnabled = true
            }
        }

        /*input_search.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.action == KeyEvent.ACTION_DOWN || keyEvent.action == KeyEvent.KEYCODE_ENTER) {
                //execute our method for searching
                hideSoftKeyboard()
                geoLocate()
            }
            hideSoftKeyboard()
            false
        }*/
    }

    /*private fun geoLocate() {
        Log.d(TAG, "geoLocate: geolocating")

        if(!input_search.text.isBlank()) {

            val searchString: String = input_search.text.toString()
            val geocoder = Geocoder(this@MapsActivity245)
            var list: List<Address> = ArrayList()
            try {
                list = geocoder.getFromLocationName(searchString, 1)
            } catch (e: IOException) {
                Log.e(TAG, "geoLocate: IOException: " + e.message)
            }
            if (!list.isEmpty()) {
                val address: Address = list[0]
                Log.d(TAG, "geoLocate: found a location: " + address.toString())
                moveCamera(LatLng(address.latitude, address.longitude), NORMAL_ZOOM, address.getAddressLine(0))
                //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    override fun onBackPressed() {

        if(addNewLocation && !firstMarker) {
            //val latLng = getFocusCameraCoordinates()
            val latLng = this.marker.position
            val resultIntent = Intent()

            /*place = Place()
            place.placeLocation.latitude = latLng.latitude
            place.placeLocation.longitude = latLng.longitude*/

            resultIntent.putExtra("Lat", latLng.latitude)
            resultIntent.putExtra("Lng", latLng.longitude)
            resultIntent.putExtra("MyUser", user)
            resultIntent.putExtra("FromMap", true)
            setResult(Activity.RESULT_OK, resultIntent)
        }

        super.onBackPressed()
    }

    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.applicationContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    var i = 0
                    while (i < grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                        i++
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                }
            }
        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient.lastLocation.addOnCompleteListener {

            if(it.isSuccessful) {
                val location = it.result
                this.currentLocation = location
                moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude),
                        BIG_ZOOM,
                        "My Location")
            }
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        if (title != "My Location") {

            if(this.firstMarker)
                this.firstMarker = false
            else
                marker.remove()

            val options = MarkerOptions()
                .position(latLng)
                .title(title)

            marker = mMap.addMarker(options)
            btn_save_location.isEnabled = true
            btn_add_location.isEnabled = true
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap.isMyLocationEnabled = true
        }
        hideSoftKeyboard()
    }

    private fun getFocusCameraCoordinates(): LatLng {
        return mMap.cameraPosition.target
    }

    private fun hideSoftKeyboard() {
        //this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val view = this.currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d(TAG, "onMapReady: map is ready");

        //Restringimos la camara a los limites de la comunidad de madrid
        mMap.setLatLngBoundsForCameraTarget(comuMadridBounds)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madridBounds.center, 10f))

        if(addNewLocation && mLocationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap.isMyLocationEnabled = true;
            mMap.uiSettings.isMyLocationButtonEnabled = false;

            getLastKnownLocation()
        }
        else {
            val latLng = LatLng(latitude, longitude)
            moveCamera(latLng, NORMAL_ZOOM, placeName)
        }

        init()
        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }

}