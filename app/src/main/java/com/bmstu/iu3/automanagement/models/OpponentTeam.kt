package com.bmstu.iu3.automanagement.models

class OpponentTeam {
    private var name: String = ""
    private var car: Car? = null
    private var pilot: Pilot? = null
    private var engineer: Engineer? = null

    fun getName(): String = name
    fun setName(value: String) { name = value }

    fun getCar(): Car? = car
    fun setCar(value: Car?) { car = value }

    fun getPilot(): Pilot? = pilot
    fun setPilot(value: Pilot?) { pilot = value }

    fun getEngineer(): Engineer? = engineer
    fun setEngineer(value: Engineer?) { engineer = value }
}
