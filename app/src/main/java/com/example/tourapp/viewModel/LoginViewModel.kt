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
    var registerNotify: MutableLiveData<Boolean> = MutableLiveData()

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
}