package com.example.tourapp.views

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.example.tourapp.R
import com.example.tourapp.viewModel.LoginViewModel
import com.google.android.material.textfield.TextInputEditText
import android.app.AlertDialog
import android.content.Intent
import android.widget.EditText
import androidx.lifecycle.Observer
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.SharedPreferencesManager

class LoginFragment : Fragment() {

    //private val executor = Executor {}
    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var button_login: Button

    //Observador del booleano loginNotify
    lateinit var observerLogin: Observer<Boolean>

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email = view.findViewById(R.id.input_email)
        password = view.findViewById(R.id.input_password)
        button_login = view.findViewById(R.id.btn_login)

        button_login.setOnClickListener {view ->
            onLoginClick(view)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        //SharedPreferencesManager.clearAndCommitPreferences()

        observerLogin = Observer {
            if(viewModel.loginNotify.value!!)
                loginFinished(it)
        }

    }

    fun onLoginClick(v: View){

        when {
            email.text.toString().isEmpty() || password.text.toString().isEmpty()  -> {
                if (email.text.toString().isEmpty()) email.error = getString(R.string.ob_campo) else email.error = null
                if (password.text.toString().isEmpty()) password.error = getString(R.string.ob_campo) else password.error = null
            } else -> {
                //cl_loading_user.visibility = View.VISIBLE
                v.hideKeyboard()
                email.error = null
                password.error = null
            viewModel.loginData.email = email.text.toString() //Guardo el valor introducido (email) en el viewmodel
            viewModel.loginData.password = password.text.toString() //Guardo el valor introducido (password) en el viewmodel
            viewModel.onLoginClick()
            }
        }
    }

    //Ocultar teclado
    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun loginFinished(loginSuccess : Boolean) {

        if (loginSuccess){//Si hace login correctamente

            val intent = Intent(activity, MainActivity::class.java)//Entramos a pantallan principal
            //intent.putExtra(Constants.USERS, viewModel.user)
            startActivity(intent)
            activity?.finish()
            //Cerramos la actividad del login

        } else {//Sino hace login

            val dialogBuilder = AlertDialog.Builder(activity)//Mostramos alerta de error en los datos introducidos
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


    //Eliminamos observer de loginNotify y lo ponemos a false para evitar probelmas en login
    override fun onStop() {
        super.onStop()
        viewModel.loginNotify.removeObserver(observerLogin)
        viewModel.loginNotify.value = false
    }

}


