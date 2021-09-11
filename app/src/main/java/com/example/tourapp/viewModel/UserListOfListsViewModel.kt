package com.example.tourapp.viewModel

import android.app.SyncNotedAppOp
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerCustomListsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.database.*

class UserListOfListsViewModel : ViewModel() {

    var adapter: RecyclerCustomListsAdapter = RecyclerCustomListsAdapter(this)

    var listNames: ArrayList<String> = arrayListOf()
    var listElems: ArrayList<Int> = arrayListOf()
    var listCodes: ArrayList<String> = arrayListOf()

    //Solo se utilizara si se esta en modo ADMIN
    var listCreators: ArrayList<String> = arrayListOf()
    var listUserNames: ArrayList<String> = arrayListOf()

    lateinit var user: User

    var listIndex = 0
    var descargas = 0

    private lateinit var mListenerLists : ValueEventListener

    private fun setLists() {
        adapter.setLists(listNames, listCodes, listElems, listCreators, listUserNames)
        adapter.notifyDataSetChanged()
    }

    fun loadNewData() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        ref.removeEventListener(mListenerLists)

        descargas = 0
        getAllLists()
    }

    //Solo si se es ADMIN
    fun getAllLists() {
        val ref = FirebaseDatabase.getInstance().getReference(Constants.USERS)
        listNames.clear()
        listElems.clear()
        listCodes.clear()
        listCreators.clear()
        listUserNames.clear()

        mListenerLists = object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                listNames.clear()
                listElems.clear()
                listCodes.clear()
                listCreators.clear()
                listUserNames.clear()

                var totalChildren: Int = 0

                //Calculamos el numero total de listas personalizadas
                snapshot.children.forEach {
                    if(it.child(Constants.USERLISTS).exists()) {
                        val childrenNum = it.child(Constants.USERLISTS).childrenCount.toInt()
                        totalChildren += childrenNum
                    }
                }

                snapshot.children.forEach { userSnapshot ->

                    var uid = userSnapshot.key as String

                        if (userSnapshot.child(Constants.USERLISTS).exists()) {

                            userSnapshot.child(Constants.USERLISTS).children.forEach { listSnapshot ->
                                val nplaces = (listSnapshot.childrenCount - 2).toInt()
                                val key = (listSnapshot.key) as String
                                val name = listSnapshot.child(Constants.LISTNAME).value as String
                                /*val nplaces =
                                        (listSnapshot.children.elementAt(listIndex).childrenCount - 2).toInt()
                                    val key = listSnapshot.children.elementAt(listIndex).key as String
                                    val name = listSnapshot.children.elementAt(listIndex)
                                        .child(Constants.LISTNAME).value as String*/

                                listIndex++
                                descargas++

                                listNames.add(name)
                                listCodes.add(key)
                                listElems.add(nplaces)
                                listCreators.add(uid)

                                var userName = userSnapshot.child(Constants.USERNAME).value as String
                                listUserNames.add(userName)
                            }
                        }

                }

                setLists()
            }
        }

        ref.addValueEventListener(mListenerLists)
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
        val ref: DatabaseReference
        if(user.userType == Constants.ADMIN) {
            val uid = listCreators[position]
            ref = FirebaseDatabase.getInstance().getReference("${Constants.USERS}/$uid").child(Constants.USERLISTS).child(listId)
        }
        else
            ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}").child(listId)

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
        val ref: DatabaseReference
        if(user.userType == Constants.ADMIN) {
            val uid = listCreators[position]
            ref = FirebaseDatabase.getInstance().getReference("${Constants.USERS}/$uid").child(Constants.USERLISTS).child(listId)
        }
        else
            ref = FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}").child(listId)

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
        val ref = if(user.userType == Constants.ADMIN)
            FirebaseDatabase.getInstance().getReference(Constants.USERS)
        else
            FirebaseDatabase.getInstance().getReference(Constants.USERS).child("${user.userId}/${Constants.USERLISTS}")

        ref.removeEventListener(mListenerLists)
    }
}