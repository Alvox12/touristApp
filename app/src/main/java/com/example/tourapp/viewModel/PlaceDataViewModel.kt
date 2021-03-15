package com.example.tourapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.SliderAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.Comment
import com.example.tourapp.dataModel.Place
import com.example.tourapp.dataModel.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PlaceDataViewModel : ViewModel() {

    lateinit var place:Place
    lateinit var user: User
    private lateinit var mListenerComment : ValueEventListener

    var myBitmapPlaceImg: MutableMap<Int, Bitmap> = mutableMapOf()
    var sliderAdapter: SliderAdapter = SliderAdapter()
    var imagesDownloaded = MutableLiveData <Boolean>()


    fun setImagesSlider() {
        sliderAdapter.setMutableMap(myBitmapPlaceImg)
        sliderAdapter.notifyDataSetChanged()
    }

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


    fun getImages(path: String) {
        if (path != "") {

            val storageRef = FirebaseStorage.getInstance().getReference(path)
            val maxDownloadBytes: Long = 1024 * 1024

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

    fun uploadScoreUser(score: Float) {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(
            Constants.PLACECOMMENTS
        ).child(user.userId)

        placeRef.child(Constants.USERPLACESCORE).setValue(score).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_ADD_PASSWORD")
            }
            else
                Log.v("FIREBASE_BBDD", "ERROR_ADD_PASSWORD")
        }
    }


    fun deleteCommentListener() {
        val placeRef = FirebaseDatabase.getInstance().getReference(Constants.PLACES).child(place.placeId).child(
            Constants.PLACECOMMENTS
        )
        placeRef.removeEventListener(mListenerComment)
    }
}