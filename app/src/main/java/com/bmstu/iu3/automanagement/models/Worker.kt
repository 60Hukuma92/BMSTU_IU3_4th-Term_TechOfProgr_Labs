package com.bmstu.iu3.automanagement.models

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

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
    private var aggression: Int = 0 // 1 .. 100
    
    // Speeding and Jail Logic
    private var fineAmountState = mutableDoubleStateOf(0.0)
    private var fineDeadlineState = mutableIntStateOf(0) // Races remaining to pay
    private var isInJailState = mutableStateOf(false)
    private var jailSentenceState = mutableIntStateOf(0) // Races remaining in jail

    fun getAggression(): Int = aggression
    fun setAggression(value: Int) { aggression = value }

    fun getFineAmount(): Double = fineAmountState.doubleValue
    fun setFineAmount(value: Double) { fineAmountState.doubleValue = value }

    fun hasFine(): Boolean = fineAmountState.doubleValue > 0

    fun getFineDeadline(): Int = fineDeadlineState.intValue
    fun setFineDeadline(value: Int) { fineDeadlineState.intValue = value }

    fun isInJail(): Boolean = isInJailState.value
    fun setInJail(value: Boolean) { isInJailState.value = value }

    fun getJailSentence(): Int = jailSentenceState.intValue
    fun setJailSentence(value: Int) { jailSentenceState.intValue = value }
}
