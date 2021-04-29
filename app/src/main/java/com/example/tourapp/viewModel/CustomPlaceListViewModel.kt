package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerPlaceListAdapter
import com.example.tourapp.adapter.RecyclerTagListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class CustomPlaceListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerPlaceListAdapter
    lateinit var tagsAdapter: RecyclerTagListAdapter
    var listPlace: ArrayList<Place> = ArrayList()
    var keysPlaces: ArrayList<String> = arrayListOf()
    var listTags: ArrayList<String> = arrayListOf()
    var myBitmapIcon: MutableMap<Int, Bitmap> = mutableMapOf()
    private lateinit var mListenerPlace : ValueEventListener
    private lateinit var mListenerCustomList : ValueEventListener

    lateinit var listCode: String

    lateinit var user: User

    var placeIndex = 0
    var descargas = 0

    fun configAdapter() {
        myAdapter = RecyclerPlaceListAdapter(true)
        myAdapter.setCustomPlaceModel(this)
    }

    fun setPlaceList() {
        myAdapter.setPlaceList(listPlace)
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
            for(aux in listPlace) {
                if(aux.arrayTags.contains(position)) {
                    listFiltered.add(aux)
                }
            }

            setFilteredPlaceList(listFiltered)
        }
    }

    fun getTagsSelected(position: Int) {

    }

    fun getCustomListCodes() {
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}/${listCode}")
        mListenerCustomList = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               snapshot.children.forEach { code->
                   Log.v("FIREBASE_BBDD_USER", "KEY_OBTENIDA")
                   keysPlaces.add(code.value as String)
               }

                getPlaceList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }
        }

        userRef.addValueEventListener(mListenerCustomList)
    }

    fun loadNewData() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        placeRef.removeEventListener(mListenerPlace)

        descargas = 0
        getPlaceList()
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

        listPlace.clear()

        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        var placeAux: Place

        mListenerPlace = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                while(placeIndex < keysPlaces.size && descargas < Constants.MAX_DATABASE_ITEMS) {

                    val key = keysPlaces[placeIndex]
                    if (snapshot.hasChild(key)) {

                        //Descargamos el lugar
                        placeAux = getPlaceData(snapshot.child(key))
                        listPlace.add(placeAux)

                        descargas++

                        setPlaceList()
                    }
                    placeIndex++
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


    fun deleteListElem(position: Int, placeId: String) {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}/${listCode}")
        ref.child(placeId).removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                listPlace.removeAt(position)
                Log.v("FIREBASE_BBDD_USER", "EXITO AL ELIMINAR ELEMENTO")
                setPlaceList()
            }
            else
                Log.v("FIREBASE_BBDD_USER", "ERROR AL ELIMINAR ELEMENTO")
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

            pictureRef.listAll().addOnSuccessListener { result ->
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

    private fun checkUserHasAccount(userId: String, placeId: String, place: DataSnapshot) {
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)

        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(Constants.USERID) && snapshot.exists()) {
                    //Descargamos el lugar
                }
                else {
                    //Eliminamos el lugar
                    placeRef.child(placeId).removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    fun deletePlaceListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        val userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}/${listCode}")
        placeRef.removeEventListener(mListenerPlace)
        userRef.removeEventListener(mListenerCustomList)
    }
}