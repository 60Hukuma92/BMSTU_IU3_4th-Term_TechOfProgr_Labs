package com.bmstu.iu3.automanagement.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.bmstu.iu3.automanagement.models.*

object GameState {
    private val budget: MutableState<Budget> = mutableStateOf(Budget())

    private val ownedComponents = mutableStateListOf<Component>()
    private val assembledCars = mutableStateListOf<Car>()
    private val hiredEngineers = mutableStateListOf<Engineer>()
    private val hiredPilots = mutableStateListOf<Pilot>()

    private val marketComponents = mutableStateListOf<Component>().apply {
        add(Engine().apply { setName("V6 Eco"); setPrice(1500.0); setPower(450); setType("V6"); setPerformance(40.0) })
        add(Engine().apply { setName("V8 Beast"); setPrice(5000.0); setPower(800); setType("V8"); setPerformance(85.0) })
        add(Gearbox().apply { setName("6-Speed Manual"); setPrice(800.0); setType("V6"); setPerformance(30.0) })
        add(Chassis().apply { setName("Steel Tube"); setPrice(1200.0); setMaxEngineWeight(200); setSuspensionType("Double Wishbone"); setPerformance(35.0) })
    }

    private val marketEngineers = mutableStateListOf<Engineer>().apply {
        add(Engineer().apply { setName("Adrian Newey Jr."); setSkill(95); setSalary(5000.0) })
        add(Engineer().apply { setName("James Allison"); setSkill(88); setSalary(4200.0) })
        add(Engineer().apply { setName("Junior Tech"); setSkill(30); setSalary(1200.0) })
    }

    private val marketPilots = mutableStateListOf<Pilot>().apply {
        add(Pilot().apply { setName("Max Fast"); setSkill(92); setSalary(7000.0) })
        add(Pilot().apply { setName("Lewis Slow"); setSkill(85); setSalary(6500.0) })
        add(Pilot().apply { setName("Rookie Mike"); setSkill(45); setSalary(1500.0) })
    }


    fun getBudgetObject() : Budget = budget.value

    fun getMarketComponents(): List<Component> = marketComponents
    fun getMarketEngineers(): List<Engineer> = marketEngineers
    fun getMarketPilots(): List<Pilot> = marketPilots


    fun getOwnedComponents(): List<Component> = ownedComponents
    fun getAssembledCars(): List<Car> = assembledCars
    fun getHiredEngineers(): List<Engineer> = hiredEngineers
    fun getHiredPilots(): List<Pilot> = hiredPilots

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
}
