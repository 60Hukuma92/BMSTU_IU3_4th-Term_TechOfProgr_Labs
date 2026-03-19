package com.bmstu.iu3.automanagement.models

class Car {
    private var name: String = ""
    private var engine: Engine? = null
    private var gearbox: Gearbox? = null
    private var chassis: Chassis? = null
    private var suspension: Suspension? = null
    private var aerodynamics: Aerodynamics? = null
    private var tyres: Tyres? = null
    private var performance: Double = 0.0

    fun getName(): String = name
    fun setName(value: String) { name = value }

    fun getEngine(): Engine? = engine
    fun setEngine(value: Engine?) { engine = value }

    fun getGearbox(): Gearbox? = gearbox
    fun setGearbox(value: Gearbox?) { gearbox = value }

    fun getChassis(): Chassis? = chassis
    fun setChassis(value: Chassis?) { chassis = value }

    fun getSuspension(): Suspension? = suspension
    fun setSuspension(value: Suspension?) { suspension = value }

    fun getAerodynamics(): Aerodynamics? = aerodynamics
    fun setAerodynamics(value: Aerodynamics?) { aerodynamics = value }

    fun getTyres(): Tyres? = tyres
    fun setTyres(value: Tyres?) { tyres = value }
    
    fun getPerformance(): Double = performance
    fun setPerformance(value: Double) { performance = value }

    fun isComplete(): Boolean {
        return engine != null && gearbox != null && chassis != null && 
               suspension != null && aerodynamics != null && tyres != null
    }

    fun getTotalPerformance(): Double {
        var total = 0.0
        total += engine?.getPerformance() ?: 0.0
        total += gearbox?.getPerformance() ?: 0.0
        total += chassis?.getPerformance() ?: 0.0
        total += suspension?.getPerformance() ?: 0.0
        total += aerodynamics?.getPerformance() ?: 0.0
        total += tyres?.getPerformance() ?: 0.0
        return total
    }
}
