package dev.ahmedmourad.nocopy.sample

import dev.ahmedmourad.nocopy.annotations.NoCopy

@NoCopy
data class PhoneNumber private constructor(
        val value: String
) {
    companion object {
        fun of(value: String): PhoneNumber {
            return PhoneNumber(value)
        }
    }
}

fun main() {
    println(PhoneNumber.of("+201234567890")/*.copy(value = "Happy Birthday!")*/)
}
