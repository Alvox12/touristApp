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

    //Observador del booleano usuario descargado
    lateinit var observerUser: Observer<User>
    lateinit var user: User
    //lateinit var observerFinished: Observer<Boolean>

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
        observerUser = Observer {
            showUserData()
        }
        model.userNotify.observe(this, observerUser)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //showUserData()
        model.getUserData()
    }

    private fun showUserData() {
        user = model.userNotify.value!!
        //user = (activity as MainActivity).user
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