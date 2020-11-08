package com.example.tourapp.dataModel


import com.example.tourapp.commons.Constants
import java.io.Serializable

class User(
    var clientId: String = "",
    var userName: String = "",
    var userPassword: String = "",
    var userType: String = "",
    var userPhone: String = "",
    var userMail: String = "",
    var clientName: String = "",
    var userProyectCode: String = "") : Serializable {

    override fun toString(): String =
        "Id Cliente: $clientId\n" +
                "Nombre usuario: $userName\n" +
                "Tipo de usuario: $userType\n" +
                "Telefono: $userPhone\n" +
                "E-Mail: $userMail\n" +
                "Proyect Code: $userProyectCode\n" +
                "Cliente: $clientName\n"

    fun toAnyObject(): MutableMap<String, Any>{
        val user: MutableMap<String, Any> = mutableMapOf()
        user[Constants.CLIENTID] = this.clientId
        user[Constants.USERNAME] = this.userName
        user[Constants.USERPASSWORD] = this.userPassword
        user[Constants.USERTYPE] = this.userType
        user[Constants.USERPHONE] = this.userPhone
        user[Constants.USERMAIL] = this.userMail
        user[Constants.CLIENTNAME] = this.clientName
        user[Constants.PROYECT_CODE] = this.userProyectCode

        return user
    }
}