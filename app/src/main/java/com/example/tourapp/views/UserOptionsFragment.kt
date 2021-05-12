package com.example.tourapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.tourapp.R
import com.example.tourapp.dataModel.Place
import com.example.tourapp.viewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user_options.*


class UserOptionsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as MainActivity).setDrawerEnabled(false)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_options, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btn_edit_user_data.setOnClickListener {
            nav_host_fragment.view?.let { view ->
                (activity as MainActivity).useredit = (activity as MainActivity).user
                Navigation.findNavController(view).navigate(R.id.action_userOptionsFragment_to_editUserFragment)
            }
        }

        btn_delete_account.setOnClickListener {
            (activity as MainActivity).deleteAccountPopup()
        }
    }

}