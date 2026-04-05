package com.bmstu.iu3.automanagement.data

data class ComponentSaveData(
    val id: String,
    val name: String,
    val price: Double,
    val performance: Double,
    val wear: Double,
    val isDestroyed: Boolean,
    val type: String,

    val power: Int?,
    val weight: Int?,
    val gears: Int?
)

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
    val tracks: List<TrackSaveData>?,
    val raceHistory: List<List<RaceResultSaveData>>,
    val timestamp: Long
)

