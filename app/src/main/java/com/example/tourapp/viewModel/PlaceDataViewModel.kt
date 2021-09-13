package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.SliderAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.ListPlaces
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PlaceDataViewModel : ViewModel() {

    /*Almacenamos los datos de lugar*/
    lateinit var place:Place
    /*Almacenamos la longitud y latitud*/
    lateinit var latLng: LatLng
    /*Datos del usuario actual*/
    lateinit var user: User
    private lateinit var mListenerComment : ValueEventListener
    private lateinit var mListenerFav : ValueEventListener

    /*Mapa en el que se almacenan las imagenes del lugar a mostrar*/
    var myBitmapPlaceImg: MutableMap<Int, Bitmap> = mutableMapOf()
    /*Objeto en el que se muestran las imagenes*/
    var sliderAdapter: SliderAdapter = SliderAdapter()
    /*Se actualiza su valor cuendo imagnes se han descargado*/
    var imagesDownloaded = MutableLiveData <Boolean>()

    /*ID único de la lista de favoritos del usuario logueado*/
    private lateinit var keyFavList: String

    /*Indica que la lista de favoritos ha sido actualizada*/
    var favPlaceLiveData = MutableLiveData <Boolean>()
    var favoritePlace: Boolean = false


    /**Se insertan las imagenes a mostrar en pantalla*/
    fun setImagesSlider() {
        sliderAdapter.setMutableMap(myBitmapPlaceImg)
        sliderAdapter.notifyDataSetChanged()
    }

    /**Se descargan los comentarios asociados al lugar*/
    fun getCommentList() {

        place.placeComments.clear()
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(
            Constants.PLACECOMMENTS
        )
        var comentario: Comment

        mListenerComment = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEachIndexed { index, commentSnapshot ->
                    commentSnapshot.children.forEachIndexed { index2, dataSnapshot ->

                        if (dataSnapshot.key!= Constants.USERPLACESCORE){

                            val commentId = dataSnapshot.child(Constants.COMMENTID).value as String
                            val commenttxt = dataSnapshot.child(Constants.COMMENTTXT).value as String
                            val commentuserid = dataSnapshot.child(Constants.COMMENTUSERID).value as String
                            val commentusername = dataSnapshot.child(Constants.COMMENTUSERNAME).value as String

                            comentario = Comment(
                                commenttxt,
                                commentuserid,
                                commentusername,
                                commentId
                            )
                            place.placeComments.put(commentId, comentario)
                            //place.placeComments.add(index, comentario)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        placeRef.addValueEventListener(mListenerComment)
    }


    /**Se descargan las imagenes asociadas al lugar*/
    fun getImages(path: String) {
        if (path != "") {

            val bytes = Constants.ICON_MAX_SIZE4.toLong()
            val storageRef = FirebaseStorage.getInstance().getReference(path)
            val maxDownloadBytes: Long = bytes * bytes

            storageRef.listAll().addOnSuccessListener { listResult ->

                for ((index, fileRef) in listResult.items.withIndex()) {
                    fileRef.getBytes(maxDownloadBytes).addOnSuccessListener { bytes ->
                        val bmp: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        myBitmapPlaceImg[index] = bmp

                        if(index == listResult.items.lastIndex)
                            imagesDownloaded.value = true
                    }

                }
            }

        }

    }

    /**Subimos la puntuacion en formato float a las rutas especificadas en la BBDD*/
    fun uploadScoreUser(score: Float) {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(
            Constants.PLACECOMMENTS
        ).child(user.userId)

        val scoreRef = FirebaseDatabase.getInstance().getReference(Constants.PLACESCORES).child(place.placeId).child(user.userId)

        placeRef.child(Constants.USERPLACESCORE).setValue(score).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_ADD_SCORE")
                scoreRef.setValue(score).addOnSuccessListener {
                    Log.v("FIREBASE_BBDD", "SUCCESS_ADD_SCORE")
                }
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_ADD_SCORE")
        }
    }

    /**Dadas las coordenadas en formato string las convertimos a LatLng,
     * si la cadena esta vacia devolvemos 0,0*/
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

    /**
     * Se busca entre las listas del usuario en la BBDD si hay una lista de favoritos,
     * si se encuentra comprobamos si el lugar en cuestion se encuentra en la misma,
     * si no se crea una lista de favoritos vacia con un ID generado al azar*/
    fun getFavListId() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")
        mListenerFav = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var favFound = false
                snapshot.children.forEach {
                    var key = it.key
                    if (key != null) {
                        val aux = key.take(3)
                        if(aux == "FAV") {
                            //Guardamos codigo id lista
                            favFound = true
                            keyFavList = key
                        }
                    }
                }

                if(favFound) {
                    //Buscamos si el lugar está en favoritos
                    favoritePlace = snapshot.child(keyFavList).hasChild(place.placeId)
                    if(favoritePlace) {
                        favPlaceLiveData.value = true
                    }
                }
                else {
                    //Generamos id lista
                    val aux: ListPlaces = ListPlaces()
                    val auxid = aux.generateId()
                    keyFavList = "FAV${auxid}"

                    //Creamos lista
                    ref.child(keyFavList).setValue(ListPlaces(Constants.FAVLIST, keyFavList)).addOnSuccessListener { favFound = true }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        ref.addValueEventListener(mListenerFav)
    }

    /**Funcion que si el booleano upload es true agrega lugar a favoritos
     * y si es false lo elimina de la lista de favoritos*/
    fun favUploadDelete(upload: Boolean) {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")

        if (upload) { //Subimos a favoritos
            ref.child("${keyFavList}/${place.placeId}").setValue(place.placeId).addOnSuccessListener {
                favPlaceLiveData.value = true
            }
        }
        else { //Eliminamos de favoritos
            ref.child("${keyFavList}/${place.placeId}").removeValue().addOnSuccessListener {
                favPlaceLiveData.value = false
            }
        }
    }

    fun deleteCommentListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(
            Constants.PLACECOMMENTS
        )
        val favRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")
        placeRef.removeEventListener(mListenerComment)
        favRef.removeEventListener(mListenerFav)
    }
}