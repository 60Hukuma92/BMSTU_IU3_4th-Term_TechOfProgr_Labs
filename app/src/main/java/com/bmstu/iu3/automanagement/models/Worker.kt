package com.bmstu.iu3.automanagement.models

sealed class Worker {
    private var name: String = ""
    private var skill: Int = 0 // 1 .. 100
    private var salary: Double = 0.0

    fun getName(): String = name
    fun setName(value: String) { name = value }

    fun getSkill(): Int = skill
    fun setSkill(value: Int) { skill = value }

    fun getSalary(): Double = salary
    fun setSalary(value: Double) { salary = value }
}

class Engineer : Worker()

class Pilot : Worker()