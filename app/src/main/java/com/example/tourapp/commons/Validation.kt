package com.example.tourapp.commons

open class Validation {
    companion object {
        fun checkApp(s: String): Boolean {

            //añadir validacion para que no sobrepase 8 caracteres

            return checkLength(s, 1, 8)
        }

        fun checkProcess(s: String): Boolean {

            //añadir validacion para que no sobrepase 8 caracteres

            return checkLength(s, 2, 8)
        }

        fun checkComent(s: String): Boolean {

            //añadir validacion para que no sobrepase 2000 caracteres

            return checkLength(s, 3, 2000)
        }


        private fun checkLength(s: String, minLength: Int, maxLength: Int): Boolean {

            var t = false

            if (s.length >= minLength && s.length <= maxLength) t = true

            return t
        }

        fun checkMinLength(s: String, num: Int): Boolean = s.length >= num

        fun checkShameString(s0: String, s1: String): Boolean = s0 == s1

        fun checkEmptyString(s: String): Boolean = s.trim().isEmpty()

        fun checkNotShamePosSpinner(posSp: Int, pos: Int): Boolean = posSp != pos
    }
}