package com.bmstu.iu3.automanagement.models

import java.util.Locale

class Budget {
    private var amount: Double = 0.0

    fun getAmount(): Double = amount
    fun setAmount(value: Double) { amount = value }
    fun subtract(value: Double) { amount -= value }
    override fun toString(): String = String.format(Locale.US, "%.2f $", amount)
}