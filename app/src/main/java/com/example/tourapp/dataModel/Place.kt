package com.example.tourapp.dataModel

import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.RandomString
import java.io.Serializable

class Place(
    var placeId: String = "",
    var placeName: String = "",
    var placeDescription: String = "",
    var placeCreator: String = "",
    var placeScore: Int = 0,
    var placeComments: MutableList<Comment> = mutableListOf()
): Serializable {

    override fun toString(): String =
        "Nombre lugar: $placeName\nDescripcion lugar: $placeDescription\nPuntuación: $placeScore\n"

    fun toAnyObject(): MutableMap<String, Any> {
        val place: MutableMap<String, Any> = mutableMapOf()
        place[Constants.PLACENAME] = this.placeName
        place[Constants.PLACEID] = this.placeId
        place[Constants.PLACEDESCRIPTION] = this.placeDescription
        place[Constants.PLACECREATOR] = this.placeCreator
        place[Constants.PLACESCORE] = this.placeScore
        place[Constants.PLACECOMMENTS] = this.placeComments

        return place
    }

    fun generateId(): String {
        val randomString = RandomString()
        return randomString.generateId(30)
    }
}