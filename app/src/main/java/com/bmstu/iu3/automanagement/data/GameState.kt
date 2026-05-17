package com.bmstu.iu3.automanagement.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.bmstu.iu3.automanagement.models.*
import com.bmstu.iu3.automanagement.utils.MarketGenerator
import com.bmstu.iu3.automanagement.utils.OpponentGenerator

object GameState {
    private val budget: MutableState<Budget> = mutableStateOf(Budget())
    private var currentPlayer: String = "Player 1"

    private val ownedComponents = mutableStateListOf<Component>()
    private val assembledCars = mutableStateListOf<Car>()
    private val hiredEngineers = mutableStateListOf<Engineer>()
    private val hiredPilots = mutableStateListOf<Pilot>()
    private val jailedPilots = mutableStateListOf<Pilot>()
    private val opponentTeams = mutableStateListOf<OpponentTeam>()
    private val tracks = mutableStateListOf<Track>()
    private val raceHistory = mutableStateListOf<List<RaceResult>>()
    private val lastRaceCommentary = mutableStateListOf<CommentatorMessage>()

    private val marketComponents = mutableStateListOf<Component>().apply { 
        addAll(MarketGenerator.generateInitialMarket()) 
    }
    private val marketEngineers = mutableStateListOf<Engineer>().apply { 
        addAll(MarketGenerator.generateStaff().first) 
    }
    private val marketPilots = mutableStateListOf<Pilot>().apply { 
        addAll(MarketGenerator.generateStaff().second) 
    }

    init {
        resetTracksToDefault()
    }

    fun getCurrentPlayer(): String = currentPlayer
    fun setCurrentPlayer(name: String) { currentPlayer = name }

    fun getBudgetObject() : Budget = budget.value
    fun getHiredPilots(): List<Pilot> = hiredPilots
    fun getJailedPilots(): List<Pilot> = jailedPilots
    fun getAssembledCars(): List<Car> = assembledCars
    fun getTracks(): List<Track> = tracks
    fun getRaceHistory(): List<List<RaceResult>> = raceHistory
    fun getLastRaceCommentary(): List<CommentatorMessage> = lastRaceCommentary
    fun getOpponentTeams(): List<OpponentTeam> = opponentTeams
    fun getMarketEngineers(): List<Engineer> = marketEngineers
    fun getMarketPilots(): List<Pilot> = marketPilots
    fun getMarketComponents(): List<Component> = marketComponents
    fun getOwnedComponents(): List<Component> = ownedComponents
    fun getHiredEngineers(): List<Engineer> = hiredEngineers

    fun clearInventory() {
        ownedComponents.clear()
        assembledCars.clear()
        hiredEngineers.clear()
        hiredPilots.clear()
        jailedPilots.clear()
        raceHistory.clear()
        lastRaceCommentary.clear()
    }

    fun addEngineerDirectly(e: Engineer) { hiredEngineers.add(e) }
    fun addPilotDirectly(p: Pilot) { hiredPilots.add(p) }
    fun addJailedPilotDirectly(p: Pilot) { jailedPilots.add(p) }
    
    fun setTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
    }

    fun addTrack(t: Track): Boolean {
        tracks.add(t)
        return true
    }

    fun updateTrack(index: Int, t: Track): Boolean {
        if (index !in tracks.indices) return false
        tracks[index] = t
        return true
    }

    fun removeTrack(index: Int): Boolean {
        if (index !in tracks.indices || tracks.size <= 1) return false
        tracks.removeAt(index)
        return true
    }

    fun resetTracksToDefault() {
        tracks.clear()
        tracks.add(Track().apply { setName("Monza"); setLength(5.7); setStraightsRatio(0.8); setCornersRatio(0.2); setElevationChange(10.0) })
        tracks.add(Track().apply { setName("Monaco"); setLength(3.3); setStraightsRatio(0.2); setCornersRatio(0.8); setElevationChange(40.0) })
        tracks.add(Track().apply { setName("Spa"); setLength(7.0); setStraightsRatio(0.5); setCornersRatio(0.5); setElevationChange(100.0) })
    }

    fun setBudget(v: Double) { 
        budget.value.setAmount(v)
        budget.value = budget.value 
    }
    
    fun addMoney(v: Double) { 
        budget.value.setAmount(budget.value.getAmount() + v)
        budget.value = budget.value
    }

    fun spendMoney(v: Double): Boolean {
        if (budget.value.getAmount() >= v) {
            budget.value.setAmount(budget.value.getAmount() - v)
            budget.value = budget.value
            return true
        }
        return false
    }

    fun buyComponent(c: Component): Boolean {
        if (marketComponents.contains(c) && spendMoney(c.getPrice())) {
            marketComponents.remove(c)
            ownedComponents.add(c)
            return true
        }
        return false
    }

    fun hireEngineer(e: Engineer): Boolean {
        if (marketEngineers.contains(e) && spendMoney(e.getSalary())) {
            marketEngineers.remove(e)
            hiredEngineers.add(e)
            return true
        }
        return false
    }

    fun hirePilot(p: Pilot): Boolean {
        if (marketPilots.contains(p) && spendMoney(p.getSalary())) {
            marketPilots.remove(p)
            hiredPilots.add(p)
            return true
        }
        return false
    }

    fun addComponent(c: Component) { ownedComponents.add(c) }
    fun removeComponentFromInventory(c: Component) { ownedComponents.remove(c) }
    fun addCar(c: Car) { assembledCars.add(c) }
    fun removeCar(car: Car) { assembledCars.remove(car) }

    fun installComponentToCar(car: Car, component: Component): Boolean {
        if (!ownedComponents.contains(component)) return false
        val success = when(component) {
            is Engine -> { car.getEngine()?.let { ownedComponents.add(it) }; car.setEngine(component); true }
            is Gearbox -> { car.getGearbox()?.let { ownedComponents.add(it) }; car.setGearbox(component); true }
            is Chassis -> { car.getChassis()?.let { ownedComponents.add(it) }; car.setChassis(component); true }
            is Suspension -> { car.getSuspension()?.let { ownedComponents.add(it) }; car.setSuspension(component); true }
            is Aerodynamics -> { car.getAerodynamics()?.let { ownedComponents.add(it) }; car.setAerodynamics(component); true }
            is Tyres -> { car.getTyres()?.let { ownedComponents.add(it) }; car.setTyres(component); true }
            else -> false
        }
        if (success) { ownedComponents.remove(component) }
        return success
    }

    fun uninstallComponentFromCar(car: Car, component: Component): Boolean {
        val success = when(component) {
            is Engine -> if (car.getEngine() == component) { car.setEngine(null); true } else false
            is Gearbox -> if (car.getGearbox() == component) { car.setGearbox(null); true } else false
            is Chassis -> if (car.getChassis() == component) { car.setChassis(null); true } else false
            is Suspension -> if (car.getSuspension() == component) { car.setSuspension(null); true } else false
            is Aerodynamics -> if (car.getAerodynamics() == component) { car.setAerodynamics(null); true } else false
            is Tyres -> if (car.getTyres() == component) { car.setTyres(null); true } else false
            else -> false
        }
        if (success) { ownedComponents.add(component) }
        return success
    }

    fun addRaceResult(res: List<RaceResult>) { raceHistory.add(0, res) }
    fun addRaceCommentary(msgs: List<CommentatorMessage>) { lastRaceCommentary.clear(); lastRaceCommentary.addAll(msgs) }
    fun generateOpponents() { if (opponentTeams.isEmpty()) opponentTeams.addAll(OpponentGenerator.generateOpponents(9)) }
    fun aiTakeComponent(c: Component) { marketComponents.remove(c) }
    fun aiTakeEngineer(e: Engineer) { marketEngineers.remove(e) }
    fun aiTakePilot(p: Pilot) { marketPilots.remove(p) }

    fun payFine(p: Pilot) { if (spendMoney(p.getFineAmount())) { p.setFineAmount(0.0); p.setFineDeadline(0) } }
    fun releaseFromJail(p: Pilot): Boolean {
        if (spendMoney(p.getSalary() * 0.5)) { p.setInJail(false); jailedPilots.remove(p); hiredPilots.add(p); return true }
        else return false
    }

    fun processRaceEndUpdates() {
        val toJail = mutableListOf<Pilot>()
        hiredPilots.toList().forEach { p ->
            if (p.hasFine()) {
                p.setFineDeadline(p.getFineDeadline() - 1)
                if (p.getFineDeadline() <= 0) toJail.add(p)
            }
        }
        toJail.forEach { p ->
            hiredPilots.remove(p)
            p.setInJail(true); p.setJailSentence(3)
            jailedPilots.add(p)
        }
        jailedPilots.toList().forEach { p ->
            p.setJailSentence(p.getJailSentence() - 1)
            if (p.getJailSentence() <= 0) {
                p.setInJail(false); jailedPilots.remove(p); hiredPilots.add(p)
            }
        }
    }
    
    fun repairComponent(component: Component, engineer: Engineer?): Boolean {
        if (component.getWear() <= 0.0 && !component.isDestroyed()) return false
        var cost = if (component.isDestroyed()) component.getPrice() * 1.5 else component.getPrice() * 0.3 * component.getWear()
        engineer?.let { cost *= (1.0 - it.getSkill() / 200.0) }
        
        if (spendMoney(cost)) {
            component.setWear(0.0)
            component.setDestroyed(false)
            return true
        }
        return false
    }
}
