package com.example.tourapp.dataModel


import java.io.Serializable

class Comment : Serializable {
    lateinit var comment: String
    lateinit var idUser: String
    lateinit var date: String
    lateinit var time: String

    constructor()

    constructor(comentario: String, idUsuario: String) {
        this.comment = comentario
        this.idUser = idUsuario
        this.date = "1/1/2000"
        this.time = "00:00"
    }

    constructor(comentario: String, idUsuario: String,
                fecha_alta: String,hora_alta: String) {
        this.comment = comentario
        this.idUser = idUsuario
        this.date = fecha_alta
        this.time = hora_alta
    }

    override fun toString(): String {
        return "Comentario de $idUser: $comment,con fecha: $date a las $time"

    }

    fun toAnyObject(): MutableMap<String, Any>{

        val com: MutableMap<String, Any> = mutableMapOf()

        com["comment"]= this.comment!!
        com["idUser"]= this.idUser!!
        com["date"]= this.date!!
        com["time"]= this.time!!

        return com
    }

}