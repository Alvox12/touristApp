package com.example.tourapp.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var currentLocation : Location
    lateinit var mMap: GoogleMap

    var mLocationPermissionsGranted = false


    private val TAG = "MapsActivity"
    private val NORMAL_ZOOM = 15f
    private var addLocation: Boolean = false
    private lateinit var place: Place
    private lateinit var user: User

    override fun onMapReady(googleMap: GoogleMap?) {

        if (googleMap != null) {
            mMap = googleMap

            if (mLocationPermissionsGranted) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.isMyLocationEnabled = true;
                mMap.uiSettings.isMyLocationButtonEnabled = false;

                //Limite de comunidad de madrid
                val comuMadridBounds = LatLngBounds(
                        LatLng(40.227935, -4.515052),   // SW bounds
                        LatLng(41.066457, -3.127167)    // NE bounds
                )

                /*val madridBounds = LatLngBounds(
                        LatLng(40.296052, -3.935523),   // SW bounds
                        LatLng(40.543836, -3.552375)    // NE bounds
                )*/

                //Restringimos la camara a los limites de la comunidad de madrid
                mMap.setLatLngBoundsForCameraTarget(comuMadridBounds)
                getLastKnownLocation()
                //Centrar mapa en ciudad
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madridBounds.center, 10f))

                init()
            }
        }
        else
            Log.d(TAG, "Mapa no preparado")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mLocationPermissionsGranted = intent.getSerializableExtra("Permission") as Boolean
        val addPlace = intent.getSerializableExtra("AddPlace") as Boolean

        user = intent.getSerializableExtra("MyUser") as User

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(!addPlace) {
            btn_save_location.isEnabled = false
            btn_save_location.isVisible = false

            ic_gps.isEnabled = false
            ic_gps.isVisible = false

            this.place = intent.getSerializableExtra("Place") as Place
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment

        mapFragment.getMapAsync(this)
        /*if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MapsFragment.newInstance())
                .commitNow()
        }*/
    }

    private fun initMap() {
        Log.d(TAG, "initMap: initializing map")
        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(this@MapsActivity)
    }

    private fun init() {
        Log.d(TAG, "init: initializing")

        btn_save_location.setOnClickListener {
            this.addLocation = true
            onBackPressed()
        }

        input_search.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                keyEvent.action == KeyEvent.ACTION_DOWN || keyEvent.action == KeyEvent.KEYCODE_ENTER) {

                //ejecutar el metodo de busqueda
                geoLocate()
            }
            false
        })

        ic_gps.setOnClickListener {
            Log.d(TAG, "onClick: clicked gps icon")
            getLastKnownLocation()
        }

        val focus = LatLng(place.placeLatitude, place.placeLongitude)
        /*googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
        //mMap.addMarker(MarkerOptions().position(focus).title(place.placeName))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(focus))
        moveCamera(focus, NORMAL_ZOOM, place.placeName)
        //hideSoftKeyboard()
    }

    override fun onBackPressed() {

        val resultIntent = Intent()
        val bundle = Bundle()

        if(addLocation) {
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

    private fun geoLocate() {
        Log.d(TAG, "geoLocate: geolocating")
        val searchString: String = input_search.text.toString()
        val geocoder = Geocoder(this@MapsActivity)
        var list: List<Address> = ArrayList()
        try {
            list = geocoder.getFromLocationName(searchString, 1)
        } catch (e: IOException) {
            Log.e(TAG, "geoLocate: IOException: " + e.message)
        }
        if (!list.isEmpty()) {
            val address: Address = list[0]
            Log.d(TAG, "geoLocate: found a location: $address")
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(
                LatLng(address.latitude, address.longitude), NORMAL_ZOOM,
                address.getAddressLine(0)
            )
        }
    }


    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient.lastLocation.addOnCompleteListener {

            if(it.isSuccessful) {
                val location = it.result
                this.currentLocation = location
                moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude),
                        NORMAL_ZOOM,
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
        hideSoftKeyboard()
    }


    private fun getFocusCameraCoordinates(): LatLng {
        return mMap.cameraPosition.target
    }

    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }


}