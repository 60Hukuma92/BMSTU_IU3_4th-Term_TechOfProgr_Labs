package com.bmstu.iu3.automanagement.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.utils.MarketGenerator
import com.bmstu.iu3.automanagement.utils.OpponentGenerator

object GameState {
    private val budget: MutableState<Budget> = mutableStateOf(Budget())

    private val ownedComponents = mutableStateListOf<Component>()
    private val assembledCars = mutableStateListOf<Car>()
    private val hiredEngineers = mutableStateListOf<Engineer>()
    private val hiredPilots = mutableStateListOf<Pilot>()
    private val opponentTeams = mutableStateListOf<OpponentTeam>()

    private val marketComponents = mutableStateListOf<Component>().apply {
        addAll(MarketGenerator.generateInitialMarket())
    }

    private val marketEngineers = mutableStateListOf<Engineer>().apply {
        val (engineers, _) = MarketGenerator.generateStaff()
        addAll(engineers)
    }

    private val marketPilots = mutableStateListOf<Pilot>().apply {
        val (_, pilots) = MarketGenerator.generateStaff()
        addAll(pilots)
    }

    fun getBudgetObject() : Budget = budget.value
    fun getMarketComponents(): List<Component> = marketComponents
    fun getMarketEngineers(): List<Engineer> = marketEngineers
    fun getMarketPilots(): List<Pilot> = marketPilots

    fun getOwnedComponents(): List<Component> = ownedComponents
    fun getAssembledCars(): List<Car> = assembledCars
    fun getHiredEngineers(): List<Engineer> = hiredEngineers
    fun getHiredPilots(): List<Pilot> = hiredPilots

    fun getOpponentTeams(): List<OpponentTeam> = opponentTeams

    fun addCar(car: Car) { assembledCars.add(car) }
    fun addComponent(component: Component) { ownedComponents.add(component) }
    fun removeComponentFromInventory(component: Component) { ownedComponents.remove(component) }

    fun setBudget(amount: Double) {
        val currentBudget = budget.value
        currentBudget.setAmount(amount)
        budget.value = Budget().apply { setAmount(currentBudget.getAmount()) }
    }


    fun buyComponent(component: Component): Boolean {
        if (spendMoney(component.getPrice())) {
            marketComponents.remove(component)
            ownedComponents.add(component)
            return true
        }
        return false
    }

    fun hireEngineer(engineer: Engineer): Boolean {
        if (spendMoney(engineer.getSalary())) {
            marketEngineers.remove(engineer)
            hiredEngineers.add(engineer)
            return true
        }
        return false
    }

    fun hirePilot(pilot: Pilot): Boolean {
        if (spendMoney(pilot.getSalary())) {
            marketPilots.remove(pilot)
            hiredPilots.add(pilot)
            return true
        }
        return false
    }

    // AI
    fun aiTakeComponent(component: Component) { marketComponents.remove(component) }
    fun aiTakeEngineer(engineer: Engineer) { marketEngineers.remove(engineer) }
    fun aiTakePilot(pilot: Pilot) { marketPilots.remove(pilot) }

    fun generateOpponents() {
        if (opponentTeams.isEmpty()) {
            opponentTeams.addAll(OpponentGenerator.generateOpponents(9))
        }
    }

    fun spendMoney(amount: Double): Boolean {
        if (canAfford(amount)) {
            val currentBudget = budget.value
            currentBudget.subtract(amount)
            budget.value = Budget().apply { setAmount(currentBudget.getAmount()) }
            return true
        }
        return false
    }

    fun canAfford(price: Double): Boolean = budget.value.getAmount() >= price

    fun repairComponent(component: Component, engineer: Engineer?): Boolean {
        if (component.getWear() <= 0.0) return false
        var cost = component.getPrice() * 0.3 * component.getWear()
        engineer?.let {
            val discount = it.getSkill() / 200.0
            cost *= (1.0 - discount)
        }
        if (spendMoney(cost)) {
            component.setWear(0.0)
            val index = ownedComponents.indexOf(component)
            if (index != -1) { ownedComponents[index] = component }
            return true
        }
        return false
    }
}
