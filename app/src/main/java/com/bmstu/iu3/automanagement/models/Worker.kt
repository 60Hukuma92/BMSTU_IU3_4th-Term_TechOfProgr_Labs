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

class Pilot : Worker() {
    private var fineAmount: Double = 0.0
    private var fineDeadlineRaces: Int = 0 // Сколько гонок осталось на оплату
    private var jailSentenceRaces: Int = 0 // Сколько гонок осталось сидеть в тюрьме

    fun getFineAmount(): Double = fineAmount
    fun setFineAmount(value: Double) { fineAmount = value }

    fun getFineDeadline(): Int = fineDeadlineRaces
    fun setFineDeadline(value: Int) { fineDeadlineRaces = value }

    fun getJailSentence(): Int = jailSentenceRaces
    fun setJailSentence(value: Int) { jailSentenceRaces = value }

    fun hasFine(): Boolean = fineAmount > 0
    fun isInJail(): Boolean = jailSentenceRaces > 0
}
