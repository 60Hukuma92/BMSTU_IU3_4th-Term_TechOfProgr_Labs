package com.bmstu.iu3.automanagement.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bmstu.iu3.automanagement.models.Budget
import com.bmstu.iu3.automanagement.models.Component

object GameState {
    private val budget: MutableState<Budget> = mutableStateOf(Budget())

    private val ownedComponents: MutableList<Component> = mutableListOf()


//    private val cars: MutableList<Car>
//
//    private val engineers: MutableList<Engineer>
//
//    private val pilots: MutableList<Pilot>
//
//    private val raceHistory: MutableList<RaceResult>
//
//    private val opponents: MutableList<OpponentTeam>
    fun getBudgetObject() : Budget = budget.value

    fun canAfford(price: Double): Boolean = budget.value.getAmount() >= price

    fun updateBudget(newAmount: Double) {
        val currentBudget = budget.value
        currentBudget.setAmount(newAmount)
        budget.value = currentBudget // Trigger update
    }

    fun addMoney(amount: Double) {
        val currentAmount = budget.value.getAmount()
        updateBudget(currentAmount + amount)
    }

    fun spendMoney(amount: Double): Boolean {
        val currentAmount = budget.value.getAmount()
        if (currentAmount >= amount) {
            updateBudget(currentAmount - amount)
            return true
        }
        return false
    }
}
