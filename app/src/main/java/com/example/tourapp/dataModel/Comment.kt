package com.example.tourapp.dataModel


import com.example.tourapp.commons.RandomString
import java.io.Serializable
import java.security.SecureRandom


class Comment : Serializable {
    lateinit var comment: String
    lateinit var idComment: String
    lateinit var idUser: String
    lateinit var nameUser: String
    lateinit var date: String
    lateinit var time: String

    constructor()

    constructor(comentario: String, idUsuario: String, nameUsuario: String) {
        this.comment = comentario
        this.idUser = idUsuario
        this.nameUser = nameUsuario
        this.date = "1/1/2000"
        this.time = "00:00"
    }

    constructor(comentario: String, idUsuario: String, nameUsuario: String,
                fecha_alta: String, hora_alta: String) {
        this.comment = comentario
        this.idUser = idUsuario
        this.nameUser = nameUsuario
        this.date = fecha_alta
        this.time = hora_alta
    }

    override fun toString(): String {
        return "Comentario de $nameUser ($idUser),con fecha: $date a las $time"
    }

    fun toAnyObject(): MutableMap<String, Any>{

        val com: MutableMap<String, Any> = mutableMapOf()

        com["comment"]= this.comment!!
        com["idUser"]= this.idUser!!
        com["date"]= this.date!!
        com["time"]= this.time!!

        return com
    }

    fun generateId(): String {
        val randomString = RandomString()
        return randomString.generateId(16)
    }

}