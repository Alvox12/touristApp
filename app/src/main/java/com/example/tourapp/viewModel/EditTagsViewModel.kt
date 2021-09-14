package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerRegisterTagsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.database.FirebaseDatabase

class EditTagsViewModel: ViewModel() {

    /*Se guardan los datos del usuario*/
    lateinit var user: User
    /*Se guarda la lista completa de tags*/
    var arrayTags: ArrayList<String> = arrayListOf()
    /*Se guardan los tags seleccionados por el usuario*/
    var listTagsSelected: ArrayList<Boolean>? = arrayListOf()
    lateinit var myAdapter: RecyclerRegisterTagsAdapter

    var tagsEdited: MutableLiveData<Boolean> = MutableLiveData()

    fun configAdapter() {
        myAdapter = RecyclerRegisterTagsAdapter(arrayTags)
        myAdapter.notifyDataSetChanged()
    }

    /**Se dan de alta la lista de preferencias seleccionadas por el usuario a la base de datos*/
    fun uploadUserPrefs() {
        val prefRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child(user.userId).child(
            Constants.USERPREFS)

        val msg = getMsg()
        user.userPrefs = msg
        user.arrayPrefs = getPrefs(msg)

        prefRef.setValue(msg).addOnCompleteListener {
            if(it.isSuccessful) {
                tagsEdited.value = true
                Log.v("FIREBASE_BBDD", "SUCCESS_TAGS_UPLOAD")
            }
            else {
                tagsEdited.value = false
            }
        }
    }

    /**Se toman los valores numericos del formato string y se añaden a un array de enteros*/
    private fun getPrefs(prefs: String): ArrayList<Int> {
        val listInt: ArrayList<Int> = arrayListOf()

        if(prefs != "") {
            val list: List<String> = prefs.split(",")
            list.forEach { str ->
                listInt.add(Integer.parseInt(str))
            }
        }
        return listInt
    }

    /**Se toman las preferencias seleccionadas por el usuario y se convierten a string*/
    private fun getMsg(): String {
        val selected = myAdapter.getTagsSelected()
        var msg = ""
        var firstTag = true
        for((index, elem) in arrayTags.withIndex()) {
            if(selected?.get(index) == true) {
                if(firstTag) {
                    firstTag = false
                    msg += index
                }
                else {
                    msg += ",${index}"
                }
            }
        }

        return msg
    }

}