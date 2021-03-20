package com.example.tourapp.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps245.*


class MapsActivity245 : AppCompatActivity(), OnMapReadyCallback {

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
    private lateinit var place: Place
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps245)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        addNewLocation = intent.getSerializableExtra("AddNewPlace") as Boolean
        user = intent.getSerializableExtra("MyUser") as User

        if(!addNewLocation) {
            btn_save_location.isEnabled = false
            btn_save_location.visibility = View.GONE

            this.place = intent.getSerializableExtra("Place") as Place
        }

        btn_gps.setOnClickListener {
            if(mLocationPermissionsGranted) {
                getLastKnownLocation()
            }
        }

        btn_save_location.setOnClickListener {
            onBackPressed()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
        getLocationPermission()
    }

    override fun onBackPressed() {

        val resultIntent = Intent()
        val bundle = Bundle()

        if(addNewLocation) {
            val latLng = getFocusCameraCoordinates()
            place.placeLatitude = latLng.latitude
            place.placeLongitude = latLng.longitude
        }

        bundle.putSerializable("Place", place)

        resultIntent.putExtra("Place", bundle)
        resultIntent.putExtra("MyUser", user)
        resultIntent.putExtra("FromMap", true)
        setResult(123, resultIntent)
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
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        if (title != "My Location") {
            val options = MarkerOptions()
                .position(latLng)
                .title(title)
            mMap.addMarker(options)
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
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
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

        //Limite de comunidad de madrid
        val comuMadridBounds = LatLngBounds(
                LatLng(40.227935, -4.515052),   // SW bounds
                LatLng(41.066457, -3.127167)    // NE bounds
        )

        //Limite madrid ciudad
        val madridBounds = LatLngBounds(
                LatLng(40.296052, -3.935523),   // SW bounds
                LatLng(40.543836, -3.552375)    // NE bounds
        )

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
            val latLng = LatLng(place.placeLatitude, place.placeLongitude)
            moveCamera(latLng, NORMAL_ZOOM, place.placeName)
        }

        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }

}