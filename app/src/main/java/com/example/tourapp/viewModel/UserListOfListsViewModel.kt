package com.example.tourapp.viewModel

import android.app.SyncNotedAppOp
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCustomListsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListOfListsViewModel : ViewModel() {

    var adapter: RecyclerCustomListsAdapter = RecyclerCustomListsAdapter()

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

        mListenerLists = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEach { snap1->
                    val nplaces = (snap1.childrenCount-1).toInt()
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


    fun deleteListener() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")
        ref.removeEventListener(mListenerLists)
    }
}