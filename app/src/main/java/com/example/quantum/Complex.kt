package com.example.quantum

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Complex(val re: Double, val im: Double) {
    operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
    operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
    operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
    operator fun times(scalar: Double) = Complex(re * scalar, im * scalar)
    operator fun div(scalar: Double) = Complex(re / scalar, im / scalar)

    fun normSq(): Double = re * re + im * im
    fun abs(): Double = sqrt(normSq())

    companion object {
        val ZERO = Complex(0.0, 0.0)
        val ONE = Complex(1.0, 0.0)
        val I = Complex(0.0, 1.0)

        /**
         * e^{i * theta} = cos(theta) + i * sin(theta)
         */
        fun expI(theta: Double): Complex = Complex(cos(theta), sin(theta))
    }
}
