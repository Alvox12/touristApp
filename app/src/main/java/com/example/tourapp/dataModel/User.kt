package com.example.tourapp.dataModel


import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.RandomString
import java.io.Serializable

class User(
    var userName: String = "",
    var userPassword: String = "",
    var userType: String = "",
    var userMail: String = "",
    var userId: String = "",
    var userPrefs: String = ""
) : Serializable {

    var arrayPrefs: ArrayList<Int> = arrayListOf()

    override fun toString(): String =
        //"Id Cliente: $clientId\n" +
                "Nombre usuario: $userName\n" +
                "Tipo de usuario: $userType\n" +
                "E-Mail: $userMail\n"

    fun toAnyObject(): MutableMap<String, Any>{
        val user: MutableMap<String, Any> = mutableMapOf()
        user[Constants.USERNAME] = this.userName
        user[Constants.USERPASSWORD] = this.userPassword
        user[Constants.USERTYPE] = this.userType
        user[Constants.USERMAIL] = this.userMail
        user[Constants.USERID] = this.userId
        user[Constants.USERPREFS] = this.userPrefs

        return user
    }

    fun generateId(): String {
        val randomString = RandomString()
        return randomString.generateId(28)
    }
}