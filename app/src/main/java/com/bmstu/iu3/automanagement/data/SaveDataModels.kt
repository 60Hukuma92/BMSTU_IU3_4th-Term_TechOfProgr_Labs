package com.bmstu.iu3.automanagement.data

/**
 * Data classes для сериализации состояния игры
 */

// Сохраняемые данные компонента
data class ComponentSaveData(
    val id: String,
    val name: String,
    val price: Double,
    val performance: Double,
    val wear: Double,
    val isDestroyed: Boolean,
    val type: String, // Engine, Gearbox, Chassis, etc.
    // Специфичные для типа поля
    val power: Int?,
    val weight: Int?,
    val gears: Int?
)

// Сохраняемые данные машины
data class CarSaveData(
    val name: String,
    val performance: Double,
    val engine: ComponentSaveData?,
    val gearbox: ComponentSaveData?,
    val chassis: ComponentSaveData?,
    val suspension: ComponentSaveData?,
    val aerodynamics: ComponentSaveData?,
    val tyres: ComponentSaveData?
)

// Сохраняемые данные работника (инженер/пилот)
data class WorkerSaveData(
    val name: String,
    val skill: Int,
    val salary: Double,
    val type: String, // Engineer or Pilot
    // Специфичные для пилота поля
    val fineAmount: Double?,
    val fineDeadline: Int?,
    val jailSentence: Int?
)

// Сохраняемые данные результата гонки
data class RaceResultSaveData(
    val pilotName: String,
    val position: Int,
    val time: Double,
    val incidents: String // сериализованный список инцидентов
)

// Главный класс сохранённого состояния
data class GameStateSaveData(
    val playerName: String,
    val budget: Double,
    val ownedComponents: List<ComponentSaveData>,
    val assembledCars: List<CarSaveData>,
    val hiredEngineers: List<WorkerSaveData>,
    val hiredPilots: List<WorkerSaveData>,
    val jailedPilots: List<WorkerSaveData>,
    val raceHistory: List<List<RaceResultSaveData>>,
    val timestamp: Long
)

