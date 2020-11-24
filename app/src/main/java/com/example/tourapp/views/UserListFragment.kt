package com.example.tourapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tourapp.R
import com.example.tourapp.commons.BaseFragment
import com.example.tourapp.databinding.UserListFragmentBinding
import com.example.tourapp.viewModel.UserListViewModel
import kotlinx.android.synthetic.main.user_list_fragment.*

class UserListFragment : BaseFragment<UserListFragmentBinding, UserListViewModel>()  {

    companion object {
        fun newInstance() = UserListFragment()
    }

    override fun getLayoutResource(): Int = R.layout.user_list_fragment
    override fun getViewModel(): Class<UserListViewModel> = UserListViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //return inflater.inflate(R.layout.user_list_fragment, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerUser.adapter = model.adapter
        recyclerUser.layoutManager = LinearLayoutManager(view.context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model.getUserList()
    }

}