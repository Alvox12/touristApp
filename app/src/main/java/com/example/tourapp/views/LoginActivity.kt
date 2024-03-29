package com.example.tourapp.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tourapp.R
import com.example.tourapp.commons.BaseActivity
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.ActivityLoginBinding
import com.example.tourapp.viewModel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_error_login.view.*

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var linkRegister: TextView

    //Observador del booleano loginNotify
    lateinit var observerLogin: Observer<Boolean>

    //Observador del booleano usuario descargado
    lateinit var observerUser: Observer<User>
    private lateinit var user: User

    override fun getLayoutResource(): Int = R.layout.activity_login
    override fun getViewModel(): Class<LoginViewModel> = LoginViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        linkRegister = findViewById(R.id.link_register)

        /*Si se da el login descarga la informacion del usuario*/
        observerLogin = Observer {
            getUserData(it)
        }

        /*Cuando el valor de loginNotify varie ejecutar lo especificado en obseverLogin*/
        model.loginNotify = MutableLiveData()
        model.loginNotify.observe(this, observerLogin)
    }

    override fun onStart() {
        super.onStart()

        /*Opción de registro de un nuevo usuario*/
        linkRegister.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
            finish()
        }
    }


    /*Al pulsar el boton de login tomara los valores del campo de email y contraseña e intentara realizar un login, si alguno esta vacio
    * no permitira que se de este proceso*/
    fun onLoginClick(v: View){

        when {
            email.text.toString().isEmpty() || password.text.toString().isEmpty()  -> {
                if (email.text.toString().isEmpty()) tiEmail.error = getString(R.string.ob_campo) else tiEmail.error = null
                if (password.text.toString().isEmpty()) tiPassword.error = getString(R.string.ob_campo) else tiPassword.error = null
            } else -> {

            v.hideKeyboard()
            tiEmail.error = null
            tiPassword.error = null
            model.loginData.email = email.text.toString() //Guardo el valor introducido (email) en el viewmodel
            model.loginData.password = password.text.toString() //Guardo el valor introducido (password) en el viewmodel
            model.onLoginClick()
        }
        }
    }

    /*Si el proceso del login ha sido exitoso llevara a la aplicacion,
    en caso contrario mostrará ventana flotante*/
    private fun loginFinished(loginSuccess : Boolean) {

        if (loginSuccess){//Si hace login correctamente

            val intent = Intent(this, MainActivity::class.java)//Entramos a pantallan principal
            intent.putExtra("MyUser", user)
            startActivity(intent)
            finish()
            //Cerramos la actividad del login

        } else {//Si no hace login

            email.text.clear()
            password.text.clear()

            val dialogBuilder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_error_login, null)
            val b = dialogBuilder.setView(dialogView).create()

            dialogView.btnAceptarDialog.setOnClickListener {
                b.dismiss()
            }

            b.setCancelable(false)
            b.show()
        }
    }

    /*Obtiene datos del usuario*/
    private fun getUserData(loginSuccess: Boolean) {
        if(!loginSuccess)
            loginFinished(loginSuccess)
        else {
            /*Si la descarga de datos del usuario es exitosa finaliza login*/
            observerUser = Observer { user ->
                this.user = user
                loginFinished(loginSuccess)
            }
            model.userNotify.observe(this, observerUser)
            model.getUserData()
        }
    }

    //Ocultar teclado virtual
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }


    //Eliminamos observer de loginNotify y lo ponemos a false para evitar probelmas en login
    override fun onStop() {
        super.onStop()
        model.loginNotify.removeObserver(observerLogin)
        model.deleteUserListener()

        if(this::observerUser.isInitialized)
            model.userNotify.removeObserver(observerUser)

    }

}