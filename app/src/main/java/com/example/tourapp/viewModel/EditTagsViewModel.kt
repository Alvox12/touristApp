package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerRegisterTagsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.database.FirebaseDatabase

class EditTagsViewModel: ViewModel() {

    lateinit var user: User
    var arrayTags: ArrayList<String> = arrayListOf()
    var listTagsSelected: ArrayList<Boolean>? = arrayListOf()
    lateinit var myAdapter: RecyclerRegisterTagsAdapter

    fun configAdapter() {
        myAdapter = RecyclerRegisterTagsAdapter(arrayTags)
        myAdapter.notifyDataSetChanged()
    }

    fun uploadUserPrefs() {
        val prefRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child(user.userId).child(
            Constants.USERPREFS)

        val msg = getMsg()
        user.userPrefs = msg
        user.arrayPrefs = getPrefs(msg)

        prefRef.setValue(msg).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_TAGS_UPLOAD")
            }
        }
    }

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

    private fun getMsg(): String {
        val selected = myAdapter.getTagsSelected()
        var msg = ""
        var firstTag = true
        for((index, elem) in arrayTags.withIndex()) {
            if(selected?.get(index) == true) {
                if(firstTag) {
                    firstTag = false
                    msg += elem
                }
                else {
                    msg += ",${elem}"
                }
            }
        }

        return msg
    }

}