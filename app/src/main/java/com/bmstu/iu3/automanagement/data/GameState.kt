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
    
    // Race related
    private val tracks = mutableStateListOf<Track>()
    private val raceHistory = mutableStateListOf<List<RaceResult>>()

    // Рынок инициализируется один раз при старте через генератор
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

    init {
        // Initialize some tracks
        tracks.add(Track().apply { setName("Monza"); setLength(5.79); setStraightsRatio(0.7); setCornersRatio(0.3); setElevationChange(10.0) })
        tracks.add(Track().apply { setName("Monaco"); setLength(3.33); setStraightsRatio(0.2); setCornersRatio(0.8); setElevationChange(40.0) })
        tracks.add(Track().apply { setName("Spa"); setLength(7.00); setStraightsRatio(0.5); setCornersRatio(0.5); setElevationChange(100.0) })
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
    fun getTracks(): List<Track> = tracks
    fun getRaceHistory(): List<List<RaceResult>> = raceHistory

    fun addCar(car: Car) { assembledCars.add(car) }
    fun addComponent(component: Component) { ownedComponents.add(component) }
    fun removeComponentFromInventory(component: Component) { ownedComponents.remove(component) }
    fun removeCar(car: Car) { assembledCars.remove(car) }

    fun setBudget(amount: Double) {
        val currentBudget = budget.value
        currentBudget.setAmount(amount)
        budget.value = Budget().apply { setAmount(currentBudget.getAmount()) }
    }

    fun buyComponent(component: Component): Boolean {
        // Проверяем наличие компонента на рынке перед покупкой
        if (marketComponents.contains(component) && spendMoney(component.getPrice())) {
            marketComponents.remove(component)
            ownedComponents.add(component)
            return true
        }
        return false
    }

    fun hireEngineer(engineer: Engineer): Boolean {
        if (marketEngineers.contains(engineer) && spendMoney(engineer.getSalary())) {
            marketEngineers.remove(engineer)
            hiredEngineers.add(engineer)
            return true
        }
        return false
    }

    fun hirePilot(pilot: Pilot): Boolean {
        if (marketPilots.contains(pilot) && spendMoney(pilot.getSalary())) {
            marketPilots.remove(pilot)
            hiredPilots.add(pilot)
            return true
        }
        return false
    }

    fun aiTakeComponent(component: Component) { marketComponents.remove(component) }
    fun aiTakeEngineer(engineer: Engineer) { marketEngineers.remove(engineer) }
    fun aiTakePilot(pilot: Pilot) { marketPilots.remove(pilot) }

    fun generateOpponents() {
        if (opponentTeams.isEmpty()) {
            opponentTeams.addAll(OpponentGenerator.generateOpponents(9))
        }
    }

    fun addRaceResult(results: List<RaceResult>) {
        raceHistory.add(0, results) // Add to top
    }

    fun addMoney(amount: Double) {
        val currentBudget = budget.value
        currentBudget.setAmount(currentBudget.getAmount() + amount)
        budget.value = Budget().apply { setAmount(currentBudget.getAmount()) }
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
            // Trigger UI update
            val index = ownedComponents.indexOf(component)
            if (index != -1) { ownedComponents[index] = component }
            // Check in cars too
            assembledCars.forEach { car ->
                if (car.getEngine() == component) car.setEngine(component as Engine)
                if (car.getGearbox() == component) car.setGearbox(component as Gearbox)
                if (car.getChassis() == component) car.setChassis(component as Chassis)
                if (car.getSuspension() == component) car.setSuspension(component as Suspension)
                if (car.getAerodynamics() == component) car.setAerodynamics(component as Aerodynamics)
                if (car.getTyres() == component) car.setTyres(component as Tyres)
            }
            return true
        }
        return false
    }
}
