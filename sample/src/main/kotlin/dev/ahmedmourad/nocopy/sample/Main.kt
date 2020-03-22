package dev.ahmedmourad.nocopy.sample

import dev.ahmedmourad.nocopy.annotations.NoCopy

//@LeastVisibleCopy
@NoCopy
data class User private constructor(
        val name: String,
        val email: String,
        val phoneNumber: String
) {
    constructor() : this("", "", "")

    fun fcsamir() = name + email

    //        fun copy(name: String="", email: String="", phoneNumber: String="") {
//
//        }
    companion object {
        fun of(name: String, email: String, phoneNumber: String): User {
            return User(name, email, phoneNumber)
        }
    }
}

fun main() {
    println(User.of("", "", "").copy(name = "scs"))
}
