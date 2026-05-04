package com.bmstu.iu3.automanagement.race

import com.bmstu.iu3.automanagement.models.CommentatorMessage

/**
 * Итог многопоточной классической гонки.
 */
data class ClassicRaceOutcome(
    val sessionId: String,
    val standings: List<ClassicRaceStanding>,
    val commentary: List<CommentatorMessage> = emptyList()
)

data class ClassicRaceStanding(
    val participantId: String,
    val displayName: String,
    val finalProgress: Double,
    val position: Int
)


