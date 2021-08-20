package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tourapp.R
import com.example.tourapp.commons.BaseActivity
import com.example.tourapp.commons.Constants
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.ActivityRegisterBinding
import com.example.tourapp.viewModel.RegisterViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity<ActivityRegisterBinding, RegisterViewModel>() {

    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var nameuser: EditText
    lateinit var backLogin: ImageView

    private lateinit var user: User

    //Observador del booleano registerNotify
    lateinit var observerRegister: Observer<Boolean>
    //Avisa de si se puede introducir los tags
    lateinit var observerTags: Observer<Boolean>
    lateinit var observerTagsDownloaded: Observer<Boolean>

    private lateinit var mListenerTags : ValueEventListener
    private var tagsDownloaded: MutableLiveData<Boolean> = MutableLiveData()
    var arrayListTags: ArrayList<String> = arrayListOf()

    override fun getLayoutResource(): Int = R.layout.activity_register
    override fun getViewModel(): Class<RegisterViewModel> = RegisterViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        nameuser = findViewById(R.id.input_name)
        backLogin = findViewById(R.id.backToLogin)

        observerTags = Observer {
            if(model.tagLiveData.value!!) {
                getTags()
            }
        }

        model.tagLiveData.observe(this, observerTags)

        observerTagsDownloaded = Observer {
            if(tagsDownloaded.value!!) {
                showTagFragment()
            }
        }

        tagsDownloaded.observe(this, observerTagsDownloaded)

        observerRegister = Observer {
            if(model.flagCreate.value!!) {
                //loginFinished(SharedPreferencesManager.getSomeBooleanValues(Constants.SAVELOGIN))
                this.user = model.user
                registerFinished(it)
            }
        }

        model.flagCreate.observe(this, observerRegister)
    }

    override fun onStart() {
        super.onStart()

        btn_tags_select.setOnClickListener {
            //showTagFragment()
            onRegisterClick()
        }

        backLogin.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    fun onRegisterClick() {

        model.addNewUser(
            userName = input_name.text.toString(),
            userType = Constants.NORMAL,
            userMail = input_email.text.toString(),
            userPassword = input_password.text.toString()
        )

    }

    private fun registerFinished(registerSuccess: Boolean) {

        if (registerSuccess){//Si hace login correctamente

            val intent = Intent(this, MainActivity::class.java)//Entramos a pantallan principal
            intent.putExtra("MyUser", user)
            //intent.putExtra(Constants.USERS, model.user)
            startActivity(intent)
            finish()
            //Cerramos la actividad del login

        } else {//Si no hace login

            val dialogBuilder = AlertDialog.Builder(this)//Mostramos alerta de error en los datos introducidos
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_error_login, null)
            dialogBuilder.setView(dialogView)
            val b = dialogBuilder.create()
            b.show()
            b.setCancelable(false)
            val b_cerrar = dialogView.findViewById<View>(R.id.btnAceptarDialog)

            b_cerrar.setOnClickListener(){
                b.dismiss()
                //cl_loading_user.visibility = View.GONE
            }
        }

    }

    fun getTags() {
        arrayListTags.clear()
        val tagsRef = FirebaseDatabase.getInstance().getReference(Constants.ETIQUETAS)

        mListenerTags = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.v("FIREBASE_BBDD_TAGS", "ERROR AL DESCARGAR INFO")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.v("FIREBASE_BBDD_TAGS", "EXITO AL DESCARGAR INFO")

                snapshot.children.forEach {
                    //arrayListTags[it.key!!.toInt()] = it.value.toString()
                    arrayListTags.add(it.value.toString())
                }

                tagsDownloaded.value = true
            }

        }

        tagsRef.addValueEventListener(mListenerTags)
    }

    fun showTagFragment() {

        ll_register.isVisible = false
        hideSoftKeyboard()
        supportFragmentManager.beginTransaction()
                .add(R.id.frame_layout_register, RegisterTagsFragment())
                .addToBackStack(null)
                .commit()
    }

    fun getCurrentViewModel(): RegisterViewModel {
        return model
    }

    private fun hideSoftKeyboard() {
        //this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val view = this.currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    //Eliminamos observer de loginNotify y lo ponemos a false para evitar probelmas en login
    override fun onStop() {
        super.onStop()
        model.tagLiveData.removeObserver(observerTags)
        model.tagLiveData.value = false

        tagsDownloaded.removeObserver(observerTagsDownloaded)
        tagsDownloaded.value = false

        model.flagCreate.removeObserver(observerRegister)
        model.flagCreate.value = false

        val tagsRef = FirebaseDatabase.getInstance().getReference(Constants.ETIQUETAS)
        tagsRef.removeEventListener(mListenerTags)
    }
}