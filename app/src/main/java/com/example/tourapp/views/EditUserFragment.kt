package com.example.tourapp.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tourapp.R
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.Validation
import com.example.tourapp.dataModel.User
import com.example.tourapp.viewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_edit_user.*

class EditUserFragment : Fragment() {


    //Observador del booleano usuario editado
    lateinit var observerUser: Observer<Boolean>
    private lateinit var user: User
    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = (activity as MainActivity).useredit

        input_name.setText(user.userName, TextView.BufferType.EDITABLE)
        initListener()

        /*Al pulsar este boton la informacion actualizada se dara de alta en la BBDD*/
        btn_upload.setOnClickListener {
            onUploadData()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        /*Si los datos de usuario se han actualizado en la BBDD apropiadamente se actualizara el valor de userEdited,
        * se actualiza el nombre de la cabecera del menu lateral y se vuelve a la vista anterior*/
        observerUser = Observer {

            if(it) {
                (activity as MainActivity).editMenuName(input_name.text.toString())
            }

            //showUserData()
            input_name.text.clear()
            input_password.text.clear()
            input_password_sec.text.clear()
            //enableElements(true)

            (activity as MainActivity).onBackPressed()
        }
        viewModel.userEdited.observe(this, observerUser)
    }

    /**Se crea un objeto de tipo User para subir la informacion a la BBDD*/
    private fun onUploadData() {
        val userAux = User(input_name.text.toString(),input_password.text.toString(), user.userType, user.userMail, user.userId)
        val currentuser = (activity as MainActivity).user
        enableElements(false)
        viewModel.uploadUserData(userAux, user.userPassword, currentuser)
    }

    /** Se bloquean o desbloquean los elementos de la vista*/
    private fun enableElements(value: Boolean) {
        btn_upload.isEnabled = value
        tiName.isEnabled = value
        tiPassword.isEnabled = value
        tiPasswordSecure.isEnabled = value
    }

    /**
     * Inicializamos los listener de los elementos visuales*/
    private fun initListener(){

        input_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    Validation.checkMinLength(input_password.text.toString(), Constants.MAX_LENGTH_USER) -> {input_password.tag = true
                        tiPassword.isErrorEnabled=false}
                    else -> { tiPassword.error = getString(R.string.max_length) +" "+ Constants.MAX_LENGTH_USER
                        input_password.tag = false}

                }
                setShameStringError(input_password,input_password_sec)
                validateEnableButtonSignInUp()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        input_password_sec.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    Validation.checkMinLength(input_password_sec.text.toString(), Constants.MAX_LENGTH_USER) -> {
                        tiPasswordSecure.isErrorEnabled=false}
                    else -> { tiPasswordSecure.error = getString(R.string.max_length) +" "+ Constants.MAX_LENGTH_USER }
                }
                setShameStringError(input_password,input_password_sec)
                validateEnableButtonSignInUp()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    /**
     *  Funcione que valida si dos campos contienen la misma cadena
     */
    private fun setShameStringError(et0: EditText, et1: EditText){
        when {
            Validation.checkShameString(et0.text.toString(),et1.text.toString()) -> {
                tiPasswordSecure.error = null
                et1.tag = true
            }
            else -> {
                tiPasswordSecure.error = getString(R.string.shame_pass)
                et1.tag = false
            }
        }
    }

    /**
     *  Valida que todos los tags de los campos esten validados
     */
    private fun validateEnableButtonSignInUp(): Boolean {
        when {
            input_password.tag as Boolean
                    && input_password_sec.tag as Boolean -> btn_upload.isEnabled = true
            else -> btn_upload.isEnabled = false
        }
        return btn_upload.isEnabled
    }


    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
//        viewModel.deleteUserListener()
        viewModel.userEdited.removeObserver(observerUser)
        (activity as MainActivity).setDrawerEnabled(true)
    }

}