package com.bmstu.iu3.automanagement.data

data class ComponentSaveData(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val performance: Double = 0.0,
    val wear: Double = 0.0,
    val isDestroyed: Boolean = false,
    val type: String = "Unknown",
    val power: Int? = null,
    val weight: Int? = null,
    val gears: Int? = null,
    val componentType: String? = null,
    val maxEngineWeight: Int? = null,
    val suspensionType: String? = null,
    val grip: Double? = null,
    val accuracy: Double? = null,
    val impact: Int? = null,
    val range: Int? = null
)

data class CarSaveData(
    val name: String = "",
    val performance: Double = 0.0,
    val engine: ComponentSaveData?,
    val gearbox: ComponentSaveData?,
    val chassis: ComponentSaveData?,
    val suspension: ComponentSaveData?,
    val aerodynamics: ComponentSaveData?,
    val tyres: ComponentSaveData?,
    val meleeWeapon1: ComponentSaveData? = null,
    val meleeWeapon2: ComponentSaveData? = null,
    val rangedWeapon: ComponentSaveData? = null
)

data class WorkerSaveData(
    val name: String,
    val skill: Int,
    val salary: Double,
    val type: String,

    val fineAmount: Double?,
    val fineDeadline: Int?,
    val jailSentence: Int?
)


data class RaceResultSaveData(
    val pilotName: String,
    val position: Int,
    val time: Double,
    val incidents: String
)

data class TrackSaveData(
    val name: String,
    val length: Double,
    val straightsRatio: Double,
    val cornersRatio: Double,
    val elevationChange: Double
)

data class GameStateSaveData(
    val playerName: String,
    val budget: Double,
    val ownedComponents: List<ComponentSaveData>,
    val assembledCars: List<CarSaveData>,
    val hiredEngineers: List<WorkerSaveData>,
    val hiredPilots: List<WorkerSaveData>,
    val jailedPilots: List<WorkerSaveData>,
    val tracks: List<TrackSaveData>? = null,
    val raceHistory: List<List<RaceResultSaveData>>,
    val timestamp: Long
)

