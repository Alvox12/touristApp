package com.example.tourapp.viewModel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tourapp.adapter.RecyclerRegisterTagsAdapter
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterViewModel : ViewModel() {

    private  var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /*Se almacena datos usuario*/
    lateinit var user: User

    /*Variable indica que registro realizado*/
    var flagCreate: MutableLiveData<Boolean> = MutableLiveData()

    /*Variable indica que se puede pasar a vista de seleccion etiquetas*/
    var tagLiveData: MutableLiveData<Boolean> = MutableLiveData()

    /*Preferencias seleccionadas por el usuario aregistrar*/
    var arrayTags: ArrayList<String> = arrayListOf()

    /*Adaptador lista de preferencias o tags*/
    lateinit var myAdapter: RecyclerRegisterTagsAdapter

    private  var refUser: DatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.USERS)

    /**
     * Fucion para añadir usurarios
     */
    fun addNewUser(
        userName: String,
        userPassword: String,
        userType: String,
        userMail: String){

        createUser(
            userName,
            userPassword,
            userType,
            userMail)


        onSignUp()

    }

    /**
     * Funcion que crea un objeto usuario con los datos
     */
    private fun createUser(
        userName: String,
        userPassword: String,
        userType: String,
        userMail: String) {

        user = User(
            userName = userName,
            userPassword = userPassword,
            userType = userType,
            userMail = userMail
        )
    }

    fun configAdapter() {
        myAdapter = RecyclerRegisterTagsAdapter(arrayTags)
        myAdapter.notifyDataSetChanged()
    }

    /**
     * Funcion para autenticar al usuario y añadirlo en DB
     */
    private fun onSignUp() {

        if(user.userMail.isNotEmpty() && user.userPassword.isNotEmpty()) {
            mFirebaseAuth.signInAnonymously().addOnCompleteListener { result ->
                if(result.isSuccessful) {
                    tagLiveData.value = true
                    Log.v("FIREBASE_LOGIN", "EXITO, REGISTRO ANONIMO")
                }
                else {
                    tagLiveData.value = false
                    Log.v("FIREBASE_LOGIN", "ERROR, REGISTRO ANONIMO")
                }
            }
        }

    }

    /**Registramos usuario en lista de usuarios con permisos de Firebase*/
    fun createUser(userPass: String) {

        val usrPassAux = user.userPassword
        mFirebaseAuth.signOut()

        mFirebaseAuth.createUserWithEmailAndPassword(user.userMail, user.userPassword)
            .addOnCompleteListener {result ->

                if(result.isSuccessful) {
                    Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                    loginAfterSignUp(usrPassAux)
                }
                else {
                    flagCreate.value = false
                    Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                }
            }
    }

    /**
     * Tras registrar al usuario nos entramos en su cuenta.
     */
    private fun loginAfterSignUp(usrPassAux: String) {

        mFirebaseAuth?.signInWithEmailAndPassword(user.userMail, usrPassAux)
            ?.addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.v("FIREBASE_LOGIN", "SUCCESS_LOGIN")
                    uploadUserData(usrPassAux)
                }
                else {
                    Log.v("FIREBASE_LOGIN", "ERROR, BAD CREDENCIALES")
                    flagCreate.value = false
                }
            }
    }

    /**Dada de alta de datos de usuario en BBDD*/
    fun uploadUserData(userPass: String) {
        user.userPassword =  Base64.encodeToString(user.userPassword.toByteArray(), Base64.DEFAULT)
        mFirebaseAuth.uid?.let { uid ->

            user.userId = uid
            //Damos de alta el usuraio en el nodo User
            refUser.child(uid).setValue(user).addOnCompleteListener { result ->

                if (result.isSuccessful) {
                    Log.v("FIREBASE_BBDD", "SUCCESS_UPLOAD")
                    uploadUserPrefs(userPass)
                }
            }
        }
    }

    /**
     * Dar de alta preferencias seelccionadas por el usuario como un string*/
    private fun uploadUserPrefs(userPass: String) {
        val prefRef = FirebaseDatabase.getInstance().getReference(Constants.USERS).child(user.userId).child(Constants.USERPREFS)

        val msg = getMsg()
        user.userPrefs = msg
        user.arrayPrefs = getPrefs(msg)

        prefRef.setValue(msg).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.v("FIREBASE_BBDD", "SUCCESS_TAGS_UPLOAD")
                flagCreate.value = true
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
                    msg += index
                }
                else {
                    msg += ",${index}"
                }
            }
        }

        return msg
    }


    fun signOut() {
        mFirebaseAuth.signOut()
    }

}