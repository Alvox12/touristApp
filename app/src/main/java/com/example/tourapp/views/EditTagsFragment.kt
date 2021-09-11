package com.example.tourapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourapp.R
import com.example.tourapp.viewModel.EditTagsViewModel
import kotlinx.android.synthetic.main.fragment_edit_tags.*
import kotlinx.android.synthetic.main.fragment_edit_user.*


class EditTagsFragment : Fragment() {

    private lateinit var viewModel: EditTagsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var manager: RecyclerView.LayoutManager

    //Observador del booleano tags editados
    lateinit var observerTags: Observer<Boolean>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_tags, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditTagsViewModel::class.java)
        viewModel.arrayTags = (activity as MainActivity).arrayListTags
        viewModel.arrayTags.removeAt(0)
        viewModel.user = (activity as MainActivity).user

        manager = LinearLayoutManager(this.activity)
        viewModel.configAdapter()
        getSelectedTags()

        recyclerView = rv_register_tags.apply {
            layoutManager = manager
            adapter =  viewModel.myAdapter
        }

        btn_update_tags.setOnClickListener {
            rv_register_tags.isEnabled = false
            viewModel.uploadUserPrefs()
        }
    }

    override fun onStart() {
        super.onStart()

        observerTags = Observer {
            (activity as MainActivity).onBackPressed()
        }
        viewModel.tagsEdited.observe(this, observerTags)
    }

    private fun getSelectedTags() {
        val array = viewModel.user.arrayPrefs
        for(num in array.iterator()) {
            viewModel.myAdapter.listTagsSelected?.set(num, true)
        }
        viewModel.myAdapter.listTagsSelected?.let { viewModel.myAdapter.uploadSetListSelected(it) }
        viewModel.myAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.tagsEdited.removeObserver(observerTags)
        viewModel.tagsEdited.value = false
    }

}