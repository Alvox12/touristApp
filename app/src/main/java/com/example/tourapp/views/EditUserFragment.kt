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
import androidx.core.widget.addTextChangedListener
import com.example.tourapp.R
import com.example.tourapp.commons.BaseFragment
import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.Validation
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.FragmentEditUserBinding
import com.example.tourapp.databinding.FragmentUserDataBinding
import com.example.tourapp.viewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_edit_user.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditUserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditUserFragment : BaseFragment<FragmentEditUserBinding, UserViewModel>() {
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    //private var param2: String? = null

    override fun getLayoutResource(): Int = R.layout.fragment_edit_user
    override fun getViewModel(): Class<UserViewModel> = UserViewModel::class.java

    private lateinit var user: User

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as MainActivity).setDrawerEnabled(false)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = (activity as MainActivity).useredit

        input_name.setText(user.userName, TextView.BufferType.EDITABLE)
        initListener()

        btn_upload.setOnClickListener {
            onUploadData()
        }
    }


    private fun onUploadData() {
        val userAux = User(input_name.text.toString(),input_password.text.toString(),user.userType,user.userMail)
        model.uploadUserData(userAux)
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

}