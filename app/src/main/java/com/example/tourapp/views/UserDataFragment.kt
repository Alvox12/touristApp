package com.example.tourapp.views

import com.example.tourapp.R
import com.example.tourapp.commons.BaseFragment
import com.example.tourapp.databinding.FragmentUserDataBinding
import com.example.tourapp.viewModel.UserViewModel

class UserDataFragment: BaseFragment<FragmentUserDataBinding, UserViewModel>() {

    override fun getLayoutResource(): Int = R.layout.fragment_user_data
    override fun getViewModel(): Class<UserViewModel> = UserViewModel::class.java


}