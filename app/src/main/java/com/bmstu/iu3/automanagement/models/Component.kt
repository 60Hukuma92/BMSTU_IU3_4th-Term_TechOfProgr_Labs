package com.bmstu.iu3.automanagement.models

sealed class Component {
    private var id: String = ""
    private var name: String = ""
    private var price: Double = 0.0
    private var wear: Double = 0.0
    private var performance: Double = 0.0

    fun getId(): String = id
    fun setId(value: String) { id = value }
    fun getName(): String = name
    fun setName(value: String) { name = value }
    fun getPrice(): Double = price
    fun setPrice(value: Double) { price = value }
    fun getWear(): Double = wear
    fun setWear(value: Double) { wear = value }
    fun getPerformance(): Double = performance
    fun setPerformance(value: Double) { performance = value }
}

class Engine : Component() {
    private var power: Int = 0
    private var weight: Int = 0
    private var type: String = ""

    fun getPower(): Int = power
    fun setPower(value: Int) { power = value }
    fun getWeight(): Int = weight
    fun setWeight(value: Int) { weight = value }
    fun getType(): String = type
    fun setType(value: String) { type = value }
}

class Gearbox : Component() {
    private var gears: Int = 0
    private var type: String = ""

    fun getGears(): Int = gears
    fun setGears(value: Int) { gears = value }
    fun getType(): String = type
    fun setType(value: String) { type = value }
}

class Chassis : Component() {
    private var maxEngineWeight: Int = 0
    private var suspensionType: String = ""

    fun getMaxEngineWeight(): Int = maxEngineWeight
    fun setMaxEngineWeight(value: Int) { maxEngineWeight = value }
    fun getSuspensionType(): String = suspensionType
    fun setSuspensionType(value: String) { suspensionType = value }
}

class Suspension : Component() {
    private var type: String = ""
    fun getType(): String = type
    fun setType(value: String) { type = value }
}

class Aerodynamics : Component()

class Tyres : Component() {
    private var grip: Double = 0.0
    fun getGrip(): Double = grip
    fun setGrip(value: Double) { grip = value }
}
