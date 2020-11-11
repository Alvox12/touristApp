package com.example.tourapp.commons

import android.util.Base64
import java.io.UnsupportedEncodingException

class Utils {
    companion object {

        /**
         * Funcion para decodificar una cadena en Base64
         */
        fun decodeString(encoded: String): String {
            val dataDec = Base64.decode(encoded, Base64.DEFAULT)
            var decodedString = ""
            try {
                decodedString = String(dataDec)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            } finally {
                return decodedString
            }
        }

    }
}