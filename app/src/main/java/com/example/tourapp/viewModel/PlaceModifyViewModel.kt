package com.example.tourapp.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerTagListAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask

class PlaceModifyViewModel : ViewModel() {

    var arrayListTags: ArrayList<String> = arrayListOf()
    var listTagsSelected: ArrayList<Boolean>? = arrayListOf()
    lateinit var tagsAdapter: RecyclerTagListAdapter

    var myMapPlaceImg: MutableMap<Int, Uri?> = mutableMapOf()
    var myMapImgExtension: MutableMap<Int, String?> = mutableMapOf()

    private var mUploadTask: StorageTask<*>? = null

    lateinit var id_lugar: String
    lateinit var user: User
    lateinit var place: Place

    var placeUploaded = MutableLiveData <Boolean>()

    fun initData() {
        this.tagsAdapter = listTagsSelected?.let { RecyclerTagListAdapter(arrayListTags, it) }!!
    }

    fun setListAdapter() {
        //tagsAdapter.setTagsList(arrayListTags)
        tagsAdapter.notifyDataSetChanged()
    }

    fun getTagsSelected() {
        listTagsSelected = tagsAdapter.getTagsSelected()
    }

    /**Se sube a la BBDD la informacion actualizada del lugar en cuestion*/
    fun modifyPlace(place: Place) {
        val refPlace: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId)
        val comments = place.placeComments

        //Eliminamos los comentarios para que no se suban con el resto del objeto place de forma erronea
        place.placeComments.clear()

        refPlace.setValue(place).addOnCompleteListener {
            if(it.isSuccessful) {

                refPlace.child(Constants.PLACECOMMENTS).child(user.userId).setValue(comments)
                //Si no hay imagenes a subir
                if(myMapPlaceImg.isEmpty()) {
                    //Pongo observer a true para indicar que se ha terminado de subir
                    placeUploaded.value = true
                }
                else {
                    //Se suben todas las imagenes seleccionadas por el usuario
                    myMapPlaceImg.iterator().forEach { entry ->
                        uploadImage(entry.value, myMapImgExtension[entry.key], entry.key, place.placeName)
                    }
                }
            }
        }
    }

    /**
     * Subimos imagen a Firebase Storage*/
    private fun uploadImage(imagePath: Uri?, fileExtension: String?, index: Int, place_name: String) {

        if (imagePath != null) {

            //val fileName = "icon_${user.clientName.toLowerCase()}_logo.$fileExtension"
            val fileName = "img_${index}_${place_name}.$fileExtension"

            val mStorage = FirebaseStorage.getInstance().reference
            val fileReference = mStorage.child("Lugares/${id_lugar}/${fileName}")
            //val fileReference = mStorage.child("${user.clientName.toLowerCase()}_logo.$fileExtension")

            mUploadTask = fileReference.putFile(imagePath).addOnCompleteListener {

                if(it.isSuccessful) {
                    Log.d("IMAGE_UPLOAD", "Imagen subida correctamente")
                }
                else
                    Log.d("IMAGE_UPLOAD", "Error al subir imagen")


                if(index == myMapPlaceImg.size-1) {
                    //Pongo observer a true para indicar que ha sido actualizado correctamente
                    placeUploaded.value = true
                }
            }
        }
    }


}