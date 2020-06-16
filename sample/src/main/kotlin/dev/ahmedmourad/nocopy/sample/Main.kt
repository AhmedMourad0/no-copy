package dev.ahmedmourad.nocopy.sample

import dev.ahmedmourad.nocopy.annotations.LeastVisibleCopy
import dev.ahmedmourad.nocopy.annotations.NoCopy

@LeastVisibleCopy
//@Serializable
data class PhoneNumber internal constructor(
        val value: String
) {

//    fun copy(value: String): Unit {
//
//    }

    companion object {
        fun of(value: String): PhoneNumber {
            return PhoneNumber(value)
        }
    }
}

fun main() {
    println(PhoneNumber.of("+201234567890").copy(value = "Happy Birthday!"))
}
