package com.ahmedmourad.mirror.sample

import com.ahmedmourad.mirror.annotations.Mirror

//@Shatter
data class User @Mirror private constructor(
        val name: String,
        val email: String,
        val phoneNumber: String
) {
    constructor() : this("", "", "")

    internal fun x() = 1

    companion object {
        fun of(name: String, email: String, phoneNumber: String): User {
            return User(name, email, phoneNumber)
        }
    }
}
