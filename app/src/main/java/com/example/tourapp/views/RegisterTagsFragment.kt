package com.example.tourapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
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

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()

        recyclerView = rv_register_tags.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        btn_register.setOnClickListener {
            rv_register_tags.isEnabled = false
            registerUser()
        }

    }

    private fun registerUser() {
        val list = viewModel.myAdapter.getTagsSelected()
        if (list != null) {
            if(!list.contains(true)) {
                Toast.makeText((activity as RegisterActivity), "Selecciona preferencias", Toast.LENGTH_SHORT).show()
                rv_register_tags.isEnabled = true
            }
            else {
                viewModel.createUser(viewModel.user.userPassword)
            }
        }
    }

}