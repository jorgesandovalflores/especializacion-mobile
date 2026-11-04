package com.example.driver.features.home.domain.rules

import java.math.BigDecimal
import java.math.RoundingMode

object FareRules {
    const val STEP = 0.50
    const val MIN_PRICE = 1.00

    fun initial(estimatedFare: Double): Double = normalize(estimatedFare.coerceAtLeast(MIN_PRICE))

    fun increase(price: Double): Double = normalize(price.toMoney().add(STEP.toMoney()).toDouble())

    fun decrease(price: Double): Double =
        normalize(price.toMoney().subtract(STEP.toMoney()).toDouble().coerceAtLeast(MIN_PRICE))

    fun canDecrease(price: Double): Boolean = price.toMoney() > MIN_PRICE.toMoney()

    fun isValid(price: Double): Boolean = price.toMoney() >= MIN_PRICE.toMoney()

    private fun normalize(value: Double): Double = value.toMoney().toDouble()

    private fun Double.toMoney(): BigDecimal = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)
}
