package com.bmstu.iu3.automanagement.models

import com.bmstu.iu3.automanagement.utils.START_BUDGET

class Budget {
    private var amount: Double = START_BUDGET

    fun getAmount(): Double = amount
    fun setAmount(value: Double) { amount = value }
    fun subtract(value: Double) { amount -= value }
    override fun toString(): String = "Budget: %.2f$amount"
}