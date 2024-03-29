package com.example.tourapp.viewModel

import android.graphics.Bitmap
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
import java.util.*
import kotlin.collections.ArrayList

class PlaceAddViewModel : ViewModel() {

    /*Lista total de etiquetas*/
    var arrayListTags: ArrayList<String> = arrayListOf()
    /*Lista de etiquetas seleccionadas por el usuario*/
    var listTagsSelected: ArrayList<Boolean>? = arrayListOf()
    lateinit var tagsAdapter: RecyclerTagListAdapter

    /*Lista de los IDs de los lugares pertenecientes a la BBDD*/
    var listCodes: ArrayList<String> = arrayListOf()

    var myMapPlaceImg: MutableMap<Int, Uri?> = mutableMapOf()
    var myMapImgExtension: MutableMap<Int, String?> = mutableMapOf()

    private var mUploadTask: StorageTask<*>? = null

    lateinit var id_lugar: String
    lateinit var user: User
    lateinit var place: Place

    var placeUploaded = MutableLiveData <Boolean>()

    fun initData(list: ArrayList<String>) {
        arrayListTags = list.clone() as ArrayList<String>
        val selected: ArrayList<Boolean> = ArrayList(Collections.nCopies(arrayListTags.size, false))
        this.tagsAdapter = RecyclerTagListAdapter(arrayListTags, selected)
        this.id_lugar = getCodePlace()
    }

    fun setListAdapter() {
        //tagsAdapter.setTagsList(arrayListTags)
        tagsAdapter.notifyDataSetChanged()
    }

    fun getTagsSelected() {
        listTagsSelected = tagsAdapter.getTagsSelected()
    }

    /**Dar de alta lugar en BBDD*/
    fun uploadPlace(place: Place) {
        val refPlace: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.PLACES)
        refPlace.child(place.placeId).setValue(place).addOnCompleteListener {
            if(it.isSuccessful) {
                if(myMapPlaceImg.isEmpty()) {
                    //Pongo observer a true
                    placeUploaded.value = true
                }
                else {
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
                    //Pongo observer a true
                    placeUploaded.value = true
                }
            }
        }
    }

    private fun getCodePlace(): String {
        val place = Place()
        var id = place.generateId()

        while(listCodes.contains(id)) {
            id = place.generateId()
        }

        return  id
    }

}