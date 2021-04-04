package com.example.tourapp.dataModel

class PlaceCoordinates(var latitude: Double = 0.0, var longitude: Double = 0.0) {

    fun toAnyObject(): MutableMap<String, Any> {
        val location: MutableMap<String, Any> = mutableMapOf()
        location["latitude"] = latitude
        location["longitude"] = longitude

        return location
    }
}