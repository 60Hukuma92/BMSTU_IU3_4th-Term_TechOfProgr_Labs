package com.bmstu.iu3.automanagement.utils

import com.bmstu.iu3.automanagement.models.*
import kotlin.random.Random

object MarketGenerator {

    fun generateInitialMarket(): List<Component> {
        val components = mutableListOf<Component>()
        repeat(30) { components.add(generateEngine()) }
        repeat(30) { components.add(generateGearbox()) }
        repeat(30) { components.add(generateChassis()) }
        repeat(30) { components.add(generateSuspension()) }
        repeat(30) { components.add(generateAerodynamics()) }
        repeat(30) { components.add(generateTyres()) }
        return components
    }

    private fun generateEngine(): Engine = Engine().apply {
        val quality = Random.nextDouble(0.4, 1.0)
        setName(listOf("V6", "V8", "V10", "V12").random() + " " + listOf("Eco", "Sport", "Pro", "Turbo").random())
        setPrice(1000.0 + (quality * 4000.0))
        setPower((400 + (quality * 600)).toInt())
        setWeight((80 + (1.0 - quality) * 100).toInt())
        setType(if (Random.nextBoolean()) "Type-A" else "Type-B")
        setPerformance(quality * 100.0)
    }

    private fun generateGearbox(): Gearbox = Gearbox().apply {
        val type = if (Random.nextBoolean()) "Type-A" else "Type-B"
        setName("$type Trans")
        setPrice(500.0 + Random.nextDouble(1500.0))
        setType(type)
        setPerformance(Random.nextDouble(30.0, 90.0))
    }

    private fun generateChassis(): Chassis = Chassis().apply {
        val suspType = if (Random.nextBoolean()) "Active" else "Standard"
        setName("Chassis " + listOf("Alloy", "Carbon", "Titanium").random())
        setPrice(1000.0 + Random.nextDouble(4000.0))
        setMaxEngineWeight(300) 
        setSuspensionType(suspType)
        setPerformance(Random.nextDouble(40.0, 95.0))
    }

    private fun generateSuspension(): Suspension = Suspension().apply {
        val type = if (Random.nextBoolean()) "Active" else "Standard"
        setName("$type Susp")
        setPrice(400.0 + Random.nextDouble(1600.0))
        setType(type)
        setPerformance(Random.nextDouble(30.0, 85.0))
    }

    private fun generateAerodynamics(): Aerodynamics = Aerodynamics().apply {
        setName("Aero Kit " + listOf("Basic", "Adv", "Elite").random())
        setPrice(600.0 + Random.nextDouble(2000.0))
        setPerformance(Random.nextDouble(40.0, 100.0))
    }

    private fun generateTyres(): Tyres = Tyres().apply {
        setName(listOf("Hard", "Med", "Soft").random() + " Tyres")
        setPrice(300.0 + Random.nextDouble(700.0))
        setGrip(0.8 + Random.nextDouble(0.7))
        setPerformance(Random.nextDouble(50.0, 90.0))
    }

    fun generateStaff(): Pair<List<Engineer>, List<Pilot>> {
        val engineers = List(30) {
            Engineer().apply {
                setName(listOf("Eng", "Tech", "Prof").random() + "-" + Random.nextInt(1000))
                setSkill(Random.nextInt(20, 100))
                setSalary(500.0 + (getSkill() * 50.0))
            }
        }
        val pilots = List(30) {
            Pilot().apply {
                setName(listOf("Driver", "Racer", "Pro").random() + "-" + Random.nextInt(1000))
                setSkill(Random.nextInt(30, 100))
                setSalary(1000.0 + (getSkill() * 50.0))
            }
        }
        return Pair(engineers, pilots)
    }
}
