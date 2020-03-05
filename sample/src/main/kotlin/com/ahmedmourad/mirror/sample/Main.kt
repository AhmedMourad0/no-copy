package com.ahmedmourad.mirror.sample

import com.ahmedmourad.mirror.annotations.Shatter

@Shatter
data class User /*@Mirror*/ private constructor(
        val name: String,
        val email: String,
        val phoneNumber: String
) {
    constructor() : this("", "", "")

    fun fcsamir() = name + email

    //    fun copy(name: String="", email: String="", phoneNumber: String="") {
//
//    }
    companion object {
        fun of(name: String, email: String, phoneNumber: String): User {
            return User(name, email, phoneNumber)
        }
    }
}

fun main() {
    println(User.of("", "", "").copy(name = "scs"))
}
