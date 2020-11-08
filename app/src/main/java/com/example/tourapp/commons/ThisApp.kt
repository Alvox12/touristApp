package com.example.tourapp.commons

import android.app.Application
import android.content.Context
import android.view.View

class ThisApp : Application(){

    companion object {
        private var instance : ThisApp? = null

        fun getInstance(): ThisApp? {
            return instance
        }

        fun getContext(): Context? {
            return instance
        }

        var myview: View? = null
    }
    override fun onCreate() {
        instance = this
        super.onCreate()
    }


}