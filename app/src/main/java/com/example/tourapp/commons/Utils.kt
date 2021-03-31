package com.example.tourapp.commons

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        /** Comprueba si el usuario tiene permisos de lectura*/
        fun checkPermission(context: ContextWrapper): Boolean {
            var permiso: Boolean = false
            val permissionCheck = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

            if(permissionCheck == PackageManager.PERMISSION_GRANTED)
                permiso = true

            return permiso
        }

        /** Pedimos permiso a el usuario para acceder a almacenamiento*/
        fun askforPermission(v: View) {

            val permisos : Array<String> = Array(1) { Manifest.permission.READ_EXTERNAL_STORAGE }
            (v.context as AppCompatActivity).requestPermissions(permisos, Constants.MY_PERMISSIONS_REQUEST_READ_STORAGE)
        }

    }
}