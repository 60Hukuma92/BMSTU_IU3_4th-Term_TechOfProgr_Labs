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
    private val jailedPilots = mutableStateListOf<Pilot>()
    private val opponentTeams = mutableStateListOf<OpponentTeam>()

    private val tracks = mutableStateListOf<Track>()
    private val raceHistory = mutableStateListOf<List<RaceResult>>()

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
    fun getJailedPilots(): List<Pilot> = jailedPilots
    fun getOpponentTeams(): List<OpponentTeam> = opponentTeams
    fun getTracks(): List<Track> = tracks
    fun getRaceHistory(): List<List<RaceResult>> = raceHistory

    fun addCar(car: Car) { assembledCars.add(car) }
    fun addComponent(component: Component) { ownedComponents.add(component) }
    fun removeComponentFromInventory(component: Component) { ownedComponents.remove(component) }
    fun removeCar(car: Car) { assembledCars.remove(car) }

    fun clearPersonnel() {
        hiredPilots.clear()
        hiredEngineers.clear()
        jailedPilots.clear()
    }
    
    fun addPilotDirectly(pilot: Pilot) { hiredPilots.add(pilot) }
    fun addJailedPilotDirectly(pilot: Pilot) { jailedPilots.add(pilot) }

    fun setBudget(amount: Double) {
        val currentBudget = budget.value
        currentBudget.setAmount(amount)
        budget.value = Budget().apply { setAmount(currentBudget.getAmount()) }
    }

    fun buyComponent(component: Component): Boolean {
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

    fun payFine(pilot: Pilot): Boolean {
        if (pilot.hasFine() && spendMoney(pilot.getFineAmount())) {
            pilot.setFineAmount(0.0)
            pilot.setFineDeadline(0)
            return true
        }
        return false
    }

    fun releaseFromJail(pilot: Pilot): Boolean {
        val bailAmount = pilot.getSalary() * 0.5
        if (pilot.isInJail() && spendMoney(bailAmount)) {
            pilot.setJailSentence(0)
            jailedPilots.remove(pilot)
            hiredPilots.add(pilot)
            return true
        }
        return false
    }

    fun processRaceEndUpdates() {
        val toJail = mutableListOf<Pilot>()
        val fromJail = mutableListOf<Pilot>()

        hiredPilots.toList().forEach { pilot ->
            if (pilot.hasFine()) {
                pilot.setFineDeadline(pilot.getFineDeadline() - 1)
                if (pilot.getFineDeadline() <= 0) {
                    toJail.add(pilot)
                }
            }
        }

        jailedPilots.toList().forEach { pilot ->
            pilot.setJailSentence(pilot.getJailSentence() - 1)
            if (pilot.getJailSentence() <= 0) {
                fromJail.add(pilot)
            }
        }

        toJail.forEach { pilot ->
            hiredPilots.remove(pilot)
            pilot.setFineAmount(0.0)
            pilot.setJailSentence(3)
            jailedPilots.add(pilot)
        }

        fromJail.forEach { pilot ->
            jailedPilots.remove(pilot)
            hiredPilots.add(pilot)
        }
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
    }
