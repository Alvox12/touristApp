package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerPlaceListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PlaceListViewModel : ViewModel() {

    lateinit var myAdapter: RecyclerPlaceListAdapter
    var listPlace: ArrayList<Place> = ArrayList()
    var myBitmapIcon: MutableMap<Int, Bitmap> = mutableMapOf()
    private lateinit var mListenerPlace : ValueEventListener

    fun configAdapter() {
        myAdapter = RecyclerPlaceListAdapter()
    }

    fun setPlaceList() {
        myAdapter.setPlaceList(listPlace)
        myAdapter.notifyDataSetChanged()
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


                    placeAux = Place(id, name, description, creator, Integer.parseInt(score))
                    listPlace.add(placeAux)
                    setPlaceList()

                }
            }

        }

        placeRef.addValueEventListener(mListenerPlace)
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