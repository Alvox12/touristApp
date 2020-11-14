package com.example.tourapp.commons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseFragment<B: ViewDataBinding, M: ViewModel> : Fragment() {

    protected lateinit var binding: B
    protected lateinit var model: M

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding= DataBindingUtil.inflate(inflater, getLayoutResource(), container, false)
        model = ViewModelProvider(this).get(getViewModel())
        binding.setVariable(BR.model, model)

        return super.onCreateView(inflater, container, savedInstanceState)
    }


    /**
     * Dynamic resource id supply for fragment
     */
    abstract fun getLayoutResource() : Int
    abstract fun getViewModel() : Class<M>
}