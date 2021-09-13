package com.example.tourapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.tourapp.R
import com.example.tourapp.commons.BaseFragment
import com.example.tourapp.dataModel.User
import com.example.tourapp.databinding.FragmentUserDataBinding
import com.example.tourapp.viewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_user_data.*

class UserDataFragment: BaseFragment<FragmentUserDataBinding, UserViewModel>() {

    //Observador del  usuario descargado
    lateinit var observerUser: Observer<User>
    //Objeto donde se almacena la informacion del usuario
    lateinit var user: User

    override fun getLayoutResource(): Int = R.layout.fragment_user_data
    override fun getViewModel(): Class<UserViewModel> = UserViewModel::class.java


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_user_data, container, false)
    }

    override fun onStart() {
        super.onStart()
        //Permitimos al usuario interactuar con el menu lateral
        (activity as MainActivity).setDrawerEnabled(true)

        /*Una vez obtenidos los datos del usuario se muestran en pantalla*/
        observerUser = Observer {
            showUserData()
        }
        /*Al variar el valor de userNotify se invoca lo de observerUser*/
        model.userNotify.observe(this, observerUser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //showUserData()
        //Obtenemos los datos del usuario
        model.getUserData()
    }

    /**Se muestran los datos del usuario (nombre, email y tipo de usuario) en pantalla*/
    private fun showUserData() {
        user = model.userNotify.value!!

        tv_user_name.text = user.userName
        tv_user_email.text = user.userMail
        tv_user_type.text = user.userType
    }

    /**Eliminamos observer*/
    override fun onDestroyView() {
        super.onDestroyView()
        model.deleteUserListener()
        model.userNotify.removeObserver(observerUser)
    }

}