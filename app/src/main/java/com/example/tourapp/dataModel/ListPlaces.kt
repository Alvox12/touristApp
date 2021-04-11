package com.example.tourapp.dataModel

import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.RandomString
import java.io.Serializable

class ListPlaces(
        var listName: String = "",
        var listId: String = "",
        var arrayPlaces: ArrayList<String> = arrayListOf()
): Serializable {

    override fun toString(): String = "Nombre Lista: ${listName}\nNum. elementos: ${arrayPlaces.size}"

    fun toAnyObject(): MutableMap<String, Any> {
        val list: MutableMap<String, Any> = mutableMapOf()

        list[Constants.LISTNAME] = this.listName
        list[Constants.LISTID] = this.listId

        for(elem in arrayPlaces.iterator()) {
            list[elem] = elem
        }

        return list
    }

    fun generateId(): String {
        val randomString = RandomString()
        return randomString.generateId(30)
    }
}