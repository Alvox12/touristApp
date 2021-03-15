package com.example.tourapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.tourapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.android.synthetic.main.fragment_maps.*
import java.util.*

class MapsActivity : AppCompatActivity() {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var currentLocation : Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MapsFragment.newInstance())
                .commitNow()
        }

       mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Create a LatLngBounds that includes the city of Adelaide in Australia.
        val adelaideBounds = LatLngBounds(
                LatLng(-35.0, 138.58),  // SW bounds
                LatLng(-34.9, 138.61) // NE bounds
        )

        val comuMadridBounds = LatLngBounds(
                LatLng(-35.0, 138.58),  // SW bounds
                LatLng(-34.9, 138.61) // NE bounds
        )
        

        // Constrain the camera target to the Adelaide bounds.
       // map.setLatLngBoundsForCameraTarget(adelaideBounds)
    }


    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient.lastLocation.addOnCompleteListener {

            if(it.isSuccessful) {
                val location = it.result
                currentLocation = location
            }
        }
    }

}