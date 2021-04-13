package com.example.tourapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.viewModel.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_comment_list.*
import kotlinx.android.synthetic.main.fragment_register_tags.*

class RegisterTagsFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterTagsFragment()
    }

    private lateinit var viewModel: RegisterViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_tags, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        viewModel = (activity as RegisterActivity).getCurrentViewModel()
        viewModel.arrayTags = (activity as RegisterActivity).arrayListTags

        btn_register.setOnClickListener {
            registerUser()
        }

        recyclerView = recycler_comment_view.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        viewModel.configAdapter()
    }

    private fun registerUser() {
        val list = viewModel.myAdapter.getTagsSelected()
        if (list != null) {
            if(!list.contains(true)) {
                Toast.makeText((activity as RegisterActivity), "Selecciona preferencias", Toast.LENGTH_SHORT).show()
            }
            else {
                (activity as RegisterActivity).onRegisterClick()
            }
        }
    }

}