package com.example.tourapp.commons

import android.content.Context
import android.content.SharedPreferences


class SharedPreferencesManager {

    companion object{
        private var MY_PREFS : String = "loginPrefs"

        fun getSharedPreferences(): SharedPreferences {
            return ThisApp.getContext()?.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)!!
        }
        fun setSomeStringValues(dataLabel: String, dataValue: String){
            getSharedPreferences().edit().putString(dataLabel, dataValue).commit()
        }
        fun setSomeBooleanValues(dataLabel: String, dataValue: Boolean){
            getSharedPreferences().edit().putBoolean(dataLabel, dataValue).commit()
        }

        fun getSomeBooleanValues(dataLabel: String): Boolean {
            return getSharedPreferences().getBoolean(dataLabel, false)
        }

        fun getSomeStringValues(dataLabel: String): String? {
            return getSharedPreferences().getString(dataLabel, "")
        }

        fun clearAndCommitPreferences(){
            getSharedPreferences().edit().clear().commit();
        }

        fun changeFile() {
            when (MY_PREFS) {
                "loginPrefs" -> MY_PREFS = "topicPrefs"
                "topicPrefs" -> MY_PREFS = "loginPrefs"
            }
        }
    }
}