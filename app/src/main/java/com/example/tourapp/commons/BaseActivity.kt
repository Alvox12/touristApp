package com.example.tourapp.commons

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

abstract class BaseActivity<B: ViewDataBinding, M: ViewModel> : AppCompatActivity(){

    protected lateinit var binding: B
    protected lateinit var model: M

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutResource())
        model = ViewModelProvider(this).get(getViewModel())
        binding.setVariable(BR.model, model)
    }

    /**
     * Dynamic resource id supply for activity
     */
    abstract fun getLayoutResource() : Int
    abstract fun getViewModel() : Class<M>
}
