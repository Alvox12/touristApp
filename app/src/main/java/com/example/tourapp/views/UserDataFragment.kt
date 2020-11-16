package com.example.tourapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.tourapp.R
import com.example.tourapp.commons.BaseFragment
import com.example.tourapp.databinding.FragmentUserDataBinding
import com.example.tourapp.viewModel.UserViewModel

class UserDataFragment: BaseFragment<FragmentUserDataBinding, UserViewModel>() {

    override fun getLayoutResource(): Int = R.layout.fragment_user_data
    override fun getViewModel(): Class<UserViewModel> = UserViewModel::class.java


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_data, container, false)
    }

}