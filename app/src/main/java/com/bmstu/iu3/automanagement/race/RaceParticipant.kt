package com.bmstu.iu3.automanagement.race

/**
 * Участник классической гонки для многопоточного движка.
 */
data class RaceParticipant(
    val id: String,
    val displayName: String,
    val basePace: Double,
    val variance: Double
)

