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

    /*Se almacena los datos necesarios para el login*/
    var loginData : Login = Login(email = "", password = "")

    //Instancia autentificacion firebase
    val mFirebaseAuth = FirebaseAuth.getInstance()

    //Variable que notifica si el login ha sido terminado
    var loginNotify: MutableLiveData<Boolean> = MutableLiveData()

    //En la ruta especificada espera para ver si se da un evento
    private lateinit var mListenerUser : ValueEventListener

    //Variable que indica si se ha descargado la información del usuario tras el login
    var userNotify: MutableLiveData<User> = MutableLiveData()


    /*Se almacebaran los datos del usuario*/
    lateinit var user: User

    /*Con los datos del email y contraseña comprueba si son validos para acceder a los
    * servicios de Firebase*/
    fun onLoginClick(){
        mFirebaseAuth?.signInWithEmailAndPassword(loginData.email, loginData.password)
                ?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                        loginNotify.value = true
                    }
                    else {
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

                if ( p0.value !=null ){
                    SharedPreferencesManager.setSomeBooleanValues(Constants.SAVELOGIN, true)
                }
                loginNotify.value = true
            }

        })
    }

    /*Descarga de firebase tras el proceso de login los datos del usuario*/
    fun getUserData() {

        /*ID único del usuario logueado*/
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        /*Ruta donde se encuentran los datos del usuario*/
        val userRef = FirebaseDatabase.getInstance().getReference("${Constants.USERS}/$currentUser")

        mListenerUser = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_USER", "ERROR AL DESCARGAR INFO")
            }

            /*Obtencion de los datos del usuario*/
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

    /*Obtención preferencias del usuario logueado*/
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