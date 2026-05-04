package com.bmstu.iu3.automanagement.models

data class RaceSession(
    val sessionId: String,
    val trackId: String,
    val players: List<String> = emptyList(),
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val events: List<RaceLogEntry> = emptyList(),
    val tacticId: String? = null,
    val pitStopsUsed: List<String> = emptyList()
)


