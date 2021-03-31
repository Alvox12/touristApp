package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerPlaceListAdapter
import com.example.tourapp.adapter.RecyclerTagListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList


class PlaceListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerPlaceListAdapter
    lateinit var tagsAdapter: RecyclerTagListAdapter
    var listPlace: ArrayList<Place> = ArrayList()
    var listTags: ArrayList<String> = arrayListOf()
    var myBitmapIcon: MutableMap<Int, Bitmap> = mutableMapOf()
    private lateinit var mListenerPlace : ValueEventListener

    fun configAdapter() {
        myAdapter = RecyclerPlaceListAdapter()
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
                if(aux.placeTags.contains(position)) {
                    listFiltered.add(aux)
                }
            }

            setFilteredPlaceList(listFiltered)
        }
    }

    fun getTagsSelected(position: Int) {

    }

    fun getPlaceList() {

        listPlace.clear()
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        var placeAux: Place

        mListenerPlace = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEachIndexed { index, place ->

                    Log.v("FIREBASE_BBDD_USER", "EXITO AL DESCARGAR INFO")
                    val name = place.child(Constants.PLACENAME).value as String
                    val description = place.child(Constants.PLACEDESCRIPTION).value as String
                    val id = place.child(Constants.PLACEID).value as String
                    val creator = place.child(Constants.PLACECREATOR).value as String
                    val score = place.child(Constants.PLACESCORE).value as String
                    val pictures = place.child(Constants.PLACEPICTURES).value as String
                    val tags = place.child(Constants.PLACEETIQUETAS).value as String

                    val arrayTags = getTags(tags)

                    val latitude = place.child(Constants.PLACELOCATION + "/" + Constants.PLACELATITUDE).value as Double
                    val longitude = place.child(Constants.PLACELOCATION + "/" + Constants.PLACELONGITUDE).value as Double

                    //COMMENTS
                    /*var placeComments: MutableList<Comment> = mutableListOf()
                    var userid:String = ""
                    var commenttxt:String = ""
                    var comentario: Comment

                    place.child(Constants.PLACECOMMENTS).children.forEach {

                        userid = it.child(Constants.COMMENTUSER).value as String
                        commenttxt = it.child(Constants.COMMENTTXT).value as String

                        comentario = Comment(commenttxt, userid)
                        placeComments.add(comentario)
                    }*/
                    


                    /*var aux = place.child(Constants.PLACECOMMENTS).value

                    var comentario = Comment()
                    var placeComments: MutableList<Comment> = mutableListOf()
                    var coment: MutableList<Comment>? = mutableListOf()

                    var i = 0

                    for (comment in aux) {
                        Log.d("onChildAdded()","i: " + i)

                        var listaComent = (aux as ArrayList<*>).get(i)
                        Log.d("onChildAdded()","listaComent: " + listaComent)

                        comentario = Comment((listaComent as Map<*, *>)["comment"] as String,
                            listaComent["nameUser"] as String,
                            listaComent["date"] as String,
                            listaComent["time"] as String)

                        placeComments.add(comentario)

                        i++
                    }*/

                    var doubleScore = score.toDoubleOrNull()
                    if(doubleScore == null) {
                        doubleScore = 0.0
                    }

                    placeAux = Place(id, name, description, creator, doubleScore, pictures, latitude, longitude, arrayTags)
                    //placeAux = Place(id, name, description, creator, Integer.parseInt(score))
                    listPlace.add(placeAux)
                    setPlaceList()
                }
            }

        }

        placeRef.addValueEventListener(mListenerPlace)
    }

    fun getTags(aux: String): ArrayList<Int> {
        val list: List<String> = aux.split(",")
        val listInt: ArrayList<Int> = arrayListOf()

        list.forEach {str ->
            listInt.add(Integer.parseInt(str))
        }

        return listInt
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