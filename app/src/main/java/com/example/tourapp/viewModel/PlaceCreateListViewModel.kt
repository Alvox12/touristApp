package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCreateListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.ListPlaces
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class PlaceCreateListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerCreateListAdapter
    var arrayListPlaces: ArrayList<Place> = arrayListOf()
    var listPlacesSelected: ArrayList<Place>? = arrayListOf()
    private lateinit var mListenerPlace : ValueEventListener

    var listCodes : ArrayList<String> = arrayListOf()
    var newList: Boolean = true
    var listSelected: ArrayList<String> = arrayListOf()
    lateinit var listName: String
    lateinit var listId: String

    lateinit var user: User
    var listUploaded = MutableLiveData <Boolean>()

    var placeIndex = 0
    var descargas = 0

    fun configAdapter() {
        myAdapter = RecyclerCreateListAdapter(this)
    }

    fun loadNewData() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        placeRef.removeEventListener(mListenerPlace)

        descargas = 0
        getPlaceList()
    }

    fun setPlaceList() {
        myAdapter.setPlaceList(arrayListPlaces)
        myAdapter.notifyDataSetChanged()
    }

    private fun setFilteredPlaceList(listFiltered: ArrayList<Place>) {
        myAdapter.setPlaceList(listFiltered)
        myAdapter.notifyDataSetChanged()
    }

    fun filterPlaceList(position: Int) {
        if(position == 0) {
            setPlaceList()
        }
        else {
            val listFiltered: ArrayList<Place> = ArrayList()
            for(aux in arrayListPlaces) {
                if(aux.arrayTags.contains(position)) {
                    listFiltered.add(aux)
                }
            }

            setFilteredPlaceList(listFiltered)
        }
    }

    private fun getPlaceData(place: DataSnapshot): Place {

        var placeAux: Place

        val name = place.child(Constants.PLACENAME).value as String
        val description = place.child(Constants.PLACEDESCRIPTION).value as String
        val id = place.child(Constants.PLACEID).value as String
        val creator = place.child(Constants.PLACECREATOR).value as String
        val score = place.child(Constants.PLACESCORE).value
        val pictures = place.child(Constants.PLACEPICTURES).value as String
        val tags = place.child(Constants.PLACEETIQUETAS).value as String

        val coordinates = place.child(Constants.PLACECOORDINATES).value as String

        val arrayTags = getTags(tags)

        val doubleScore = score.toString()
        var scoreDouble = doubleScore.toDoubleOrNull()
        if (scoreDouble == null) {
            scoreDouble = 0.0
        }


        placeAux =  Place(id, name, description, creator, scoreDouble, pictures, coordinates, tags)
        placeAux.arrayTags = arrayTags

        return placeAux
    }


    fun getPlaceList() {

        arrayListPlaces.clear()
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        var placeAux: Place

        mListenerPlace = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                while(placeIndex < snapshot.childrenCount && descargas < Constants.MAX_DATABASE_ITEMS) {

                    //Descargamos el lugar
                    placeAux = getPlaceData(snapshot.children.elementAt(placeIndex))
                    arrayListPlaces.add(placeAux)

                    placeIndex++
                    descargas++

                    setPlaceList()
                }
                /*snapshot.children.forEachIndexed { index, place ->

                    Log.v("FIREBASE_BBDD_USER", "EXITO AL DESCARGAR INFO")
                    val name = place.child(Constants.PLACENAME).value as String
                    val description = place.child(Constants.PLACEDESCRIPTION).value as String
                    val id = place.child(Constants.PLACEID).value as String
                    val creator = place.child(Constants.PLACECREATOR).value as String
                    val score = place.child(Constants.PLACESCORE).value
                    val pictures = place.child(Constants.PLACEPICTURES).value as String
                    val tags = place.child(Constants.PLACEETIQUETAS).value as String

                    val coordinates = place.child(Constants.PLACECOORDINATES).value as String

                    //val latlng = getLatLng(coordinates)
                    val arrayTags = getTags(tags)



                    val doubleScore = score.toString()
                    var scoreDouble = doubleScore.toDoubleOrNull()
                    if(scoreDouble == null) {
                        scoreDouble = 0.0
                    }


                    placeAux = Place(id, name, description, creator, scoreDouble, pictures, coordinates, tags)
                    placeAux.arrayTags = arrayTags
                    //placeAux = Place(id, name, description, creator, Integer.parseInt(score))
                    arrayListPlaces.add(placeAux)

                    setPlaceList()
                }*/

                if(!newList) {
                    myAdapter.setSelectedPlaces()
                    myAdapter.notifyDataSetChanged()
                }
            }

        }

        placeRef.addValueEventListener(mListenerPlace)
    }


    fun getTags(aux: String): ArrayList<Int> {

        val listInt: ArrayList<Int> = arrayListOf()

        if(aux != "") {
            val list: List<String> = aux.split(",")

            list.forEach { str ->
                listInt.add(Integer.parseInt(str))
            }
        }

        return listInt
    }

    fun uploadListPlace(namePlace: String) {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")
        var id: String

        if(newList) {
            id = ListPlaces().generateId()
            while (listCodes.contains(id) && (id.take(3) == "FAV")) {
                id = ListPlaces().generateId()
            }
        }
        else
            id = listId

        val arrayCodes: ArrayList<String> = arrayListOf()
        if(listPlacesSelected != null) {
            for(elem in listPlacesSelected!!.iterator()) {
                arrayCodes.add(elem.placeId)
            }
        }

        val list = ListPlaces(namePlace, id, arrayCodes)
        ref.child(id).setValue(list.toAnyObject()).addOnCompleteListener {
            if(it.isSuccessful) {
                listUploaded.value = true
            }
        }
    }

    fun getLatLng(aux: String): LatLng {

        if(aux != "") {
            val list: List<String> = aux.split(",")
            val lat = list.first().toDoubleOrNull()
            val lng = list.last().toDoubleOrNull()

            if(lat != null && lng != null) {
                return LatLng(lat, lng)
            }
        }

        return LatLng(0.0, 0.0)
    }

    fun getFolderImages(folderDir: String) {
        if(folderDir != "") {

            val mStorage = FirebaseStorage.getInstance().reference
            val pictureRef = mStorage.child(folderDir)
            val maxDownloadBytes: Long = 1024 * 1024

            pictureRef.listAll().addOnSuccessListener {result ->
                for (fileRef in result.items) {
                    //Download the file using its reference (fileRef)
                    fileRef.getBytes(maxDownloadBytes).addOnSuccessListener { bytes ->
                        var bmp: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        //myBitmapIcon.put(index, bmp)
                    }
                }
            }

        }
    }

    fun deletePlaceListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        placeRef.removeEventListener(mListenerPlace)
    }

}