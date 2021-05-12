package com.example.tourapp.viewModel

import android.app.SyncNotedAppOp
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCustomListsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListOfListsViewModel : ViewModel() {

    var adapter: RecyclerCustomListsAdapter = RecyclerCustomListsAdapter(this)

    var listNames: ArrayList<String> = arrayListOf()
    var listElems: ArrayList<Int> = arrayListOf()
    var listCodes: ArrayList<String> = arrayListOf()

    lateinit var user: User

    private lateinit var mListenerLists : ValueEventListener

    private fun setLists() {
        adapter.setLists(listNames, listCodes, listElems)
        adapter.notifyDataSetChanged()
    }

    fun getLists() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")
        listNames.clear()
        listElems.clear()
        listCodes.clear()

        mListenerLists = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                listNames.clear()
                listElems.clear()
                listCodes.clear()

                snapshot.children.forEach { snap1->
                    val nplaces = (snap1.childrenCount-2).toInt()
                    val key = snap1.key as String
                    val name = snap1.child(Constants.LISTNAME).value as String

                    listNames.add(name)
                    listCodes.add(key)
                    listElems.add(nplaces)
                }

                setLists()
            }
        }

        ref.addValueEventListener(mListenerLists)
    }

    fun deleteList(listId: String, position: Int) {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}").child(listId)
        ref.removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_DEL_CUSTOM_USER_LIST")
                //listCodes.removeAt(position)
                //listNames.removeAt(position)
                //listElems.removeAt(position)

                setLists()
            }
            else {
                Log.v("FIREBASE_BBDD", "ERROR_DEL_CUSTOM_USER_LIST")
            }
        }
    }

    fun changeListName(listId: String, name: String, position: Int) {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}").child(listId)
        ref.child(Constants.LISTNAME).setValue(name).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_CHANGE_NAME_CUSTOM_USER_LIST")
                //listNames[position] = name
                setLists()
            }
            else {
                Log.v("FIREBASE_BBDD", "ERROR_CHANGE_NAME_CUSTOM_USER_LIST")
            }
        }
    }


    fun deleteListener() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")
        ref.removeEventListener(mListenerLists)
    }
}