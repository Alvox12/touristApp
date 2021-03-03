package com.example.tourapp.dataModel


import com.example.tourapp.commons.Constants
import com.example.tourapp.commons.RandomString
import java.io.Serializable


class Comment : Serializable {
    lateinit var commentTxt: String
    lateinit var commentId: String
    lateinit var commentUserId: String
    lateinit var commentUserName: String
    lateinit var date: String
    lateinit var time: String

    constructor()

    constructor(commentTxt: String, commentUserId: String, commentUserName: String, commentId:String) {
        this.commentTxt = commentTxt
        this.commentUserId = commentUserId
        this.commentUserName = commentUserName
        this.commentId = commentId
        this.date = "1/1/2000"
        this.time = "00:00"
    }

    constructor(commentTxt: String, commentUserId: String, commentUserName: String, commentId:String, fecha_alta: String, hora_alta: String) {
        this.commentTxt = commentTxt
        this.commentUserId = commentUserId
        this.commentUserName = commentUserName
        this.commentId = commentId
        this.date = fecha_alta
        this.time = hora_alta
    }

    override fun toString(): String {
        return "Comentario de $commentUserName ($commentUserId),con fecha: $date a las $time"
    }

    fun toAnyObject(): MutableMap<String, Any>{

        val com: MutableMap<String, Any> = mutableMapOf()

        com[Constants.COMMENTTXT]= this.commentTxt
        com[Constants.COMMENTUSERID]= this.commentUserId
        com[Constants.COMMENTUSERNAME]= this.commentUserName
        com[Constants.COMMENTID]= this.commentId
        com["date"]= this.date
        com["time"]= this.time

        return com
    }

    fun generateId(): String {
        val randomString = RandomString()
        return randomString.generateId(16)
    }

}