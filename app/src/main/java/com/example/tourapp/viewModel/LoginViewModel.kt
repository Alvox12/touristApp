package com.example.tourapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.SharedPreferencesManager
import com.example.tourapp.dataModel.Login
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class LoginViewModel : ViewModel() {

    var loginData : Login = Login(email = "", password = "")
    val mFirebaseAuth = FirebaseAuth.getInstance()
    var loginNotify: MutableLiveData<Boolean> = MutableLiveData()
    private lateinit var mListenerUser : ValueEventListener
    var userNotify: MutableLiveData<User> = MutableLiveData()


    lateinit var user: User

    fun onLoginClick(){
        /*when{
            SharedPreferencesManager.getSomeStringValues(Constants.EMAIL).toString() == loginData.email
            ->{
                loginData.email = SharedPreferencesManager
                        .getSomeStringValues(SharedPreferencesManager.getSomeStringValues(Constants.EMAIL).toString()).toString()
            }
        }*/
        mFirebaseAuth?.signInWithEmailAndPassword(loginData.email, loginData.password)
                ?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        //loadClient(mFirebaseAuth.uid.toString())
                        Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                        loginNotify.value = true
                    }
                    else {
                        //SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, false)
                        Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                        loginNotify.value = false
                    }
                }
    }

    private fun loadClient(uid: String){
        FirebaseDatabase.getInstance().getReference(Constants.USERS).orderByKey().equalTo(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, false)
                loginNotify.value = true
            }

            override fun onDataChange(p0: DataSnapshot) {
                /*p0.children.forEach {
                    user = it.getValue(User::class.java) as User
                    onSaveData(check)
                }*/

                if ( p0.value !=null ){
                    SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, true)
                }
                loginNotify.value = true
            }

        })
    }

    fun getUserData() {
        //currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userRef = FirebaseDatabase.getInstance().getReference("${Constants.USERS}/$currentUser")

        mListenerUser = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.key == currentUser) {
                    Log.v("FIREBASE_BBDD_USER", "EXITO AL DESCARGAR INFO")
                    //val user: User? = snapshot.getValue(User::class.java)
                    val email = snapshot.child(Constants.USERMAIL).value as String
                    val name = snapshot.child(Constants.USERNAME).value as String
                    val password = snapshot.child(Constants.USERPASSWORD).value as String
                    val type = snapshot.child(Constants.USERTYPE).value as String
                    val id = snapshot.child(Constants.USERID).value as String
                    val prefs = snapshot.child(Constants.USERPREFS).value as String

                    val userAux = User(name, password, type, email, id, prefs)
                    userAux.arrayPrefs = getPrefs(prefs)
                    userNotify.value = userAux
                }
                else Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

        }

        userRef.addValueEventListener(mListenerUser)
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

    fun deleteUserListener() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userRef = FirebaseDatabase.getInstance().getReference("${Constants.USERS}/$currentUser")
        if(this::mListenerUser.isInitialized)
            userRef.removeEventListener(mListenerUser)
    }

}