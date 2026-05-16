package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.CommentatorMessage
import com.bmstu.iu3.automanagement.models.Incident

data class ClassicRaceOutcome(
    val sessionId: String,
    val standings: List<ClassicRaceStanding>,
    val commentary: List<CommentatorMessage> = emptyList(),
    val pilotFines: Map<String, Double> = emptyMap() 
)

data class ClassicRaceStanding(
    val participantId: String,
    val displayName: String,
    val finalProgress: Double,
    val position: Int,
    val incident: Incident? = null
)
