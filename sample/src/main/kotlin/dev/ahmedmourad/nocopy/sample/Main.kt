package dev.ahmedmourad.nocopy.sample

import dev.ahmedmourad.nocopy.annotations.NoCopy

@NoCopy
data class PhoneNumber private constructor(
    val value: String,
    val x: Int
) {
    //    fun copy(value: String): PhoneNumber {
//        return PhoneNumber(value.toString(), 5)
//    }
    companion object {
        fun of(value: String): PhoneNumber {
            return PhoneNumber(value, 5)
        }
    }
}

fun main() {
    println(PhoneNumber.of("+201234567890").copy(value = "Happy Birthday!"))
}
